package scheduler;

import model.Process;
import result.GanttSegment;
import result.MetricsCalculator;
import result.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shortest Job First (SJF) - non-preemptive.
 *
 * Rule: among the processes that have ALREADY ARRIVED and are not yet
 * finished, always run the one with the smallest burst time next. Once
 * chosen, it runs to completion without interruption (that's what makes
 * this the non-preemptive version - see SRTFScheduler for the preemptive one).
 */
public class SJFScheduler implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "Shortest Job First (SJF - Non-Preemptive)";
    }

    @Override
    public ScheduleResult schedule(List<Process> inputProcesses) {
        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(p.copyForSimulation());
        }

        List<GanttSegment> gantt = new ArrayList<>();
        int currentTime = 0;
        int completedCount = 0;
        int n = processes.size();

        while (completedCount < n) {
            // Step 1: gather processes that have arrived and are still waiting.
            List<Process> available = new ArrayList<>();
            for (Process p : processes) {
                if (!p.isCompleted() && p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                // Step 2: CPU is idle - jump forward to the next arrival.
                int nextArrival = processes.stream()
                        .filter(p -> !p.isCompleted())
                        .mapToInt(Process::getArrivalTime)
                        .min()
                        .orElse(currentTime);
                gantt.add(new GanttSegment(GanttSegment.IDLE, currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // Step 3: pick the shortest burst time; tie-break by arrival then id
            // so results are always reproducible.
            Process next = available.stream()
                    .min(Comparator.comparingInt(Process::getBurstTime)
                            .thenComparingInt(Process::getArrivalTime)
                            .thenComparing(Process::getProcessId))
                    .orElseThrow();

            // Step 4: run it fully (non-preemptive).
            int start = currentTime;
            int completion = start + next.getBurstTime();

            next.setStartTime(start);
            next.setCompletionTime(completion);
            next.setResponseTime(start - next.getArrivalTime());
            next.setTurnaroundTime(completion - next.getArrivalTime());
            next.setWaitingTime(next.getTurnaroundTime() - next.getBurstTime());
            next.setRemainingBurstTime(0);

            gantt.add(new GanttSegment(next.getProcessId(), start, completion));
            currentTime = completion;
            completedCount++;
        }

        return new ScheduleResult(
                getName(),
                processes,
                gantt,
                MetricsCalculator.averageWaitingTime(processes),
                MetricsCalculator.averageTurnaroundTime(processes),
                MetricsCalculator.averageResponseTime(processes)
        );
    }
}
