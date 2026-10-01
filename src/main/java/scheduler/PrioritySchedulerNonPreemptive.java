package scheduler;

import model.Process;
import result.GanttSegment;
import result.MetricsCalculator;
import result.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Priority Scheduling - non-preemptive.
 *
 * Convention used throughout this project: a LOWER priority number means
 * HIGHER priority (this matches most real operating systems, e.g. Linux
 * "nice" values). Priority 1 runs before priority 5.
 *
 * Rule: among processes that have arrived and are waiting, always pick the
 * one with the best (lowest) priority number. Once chosen it runs to
 * completion without interruption.
 */
public class PrioritySchedulerNonPreemptive implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "Priority Scheduling (Non-Preemptive)";
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
            List<Process> available = new ArrayList<>();
            for (Process p : processes) {
                if (!p.isCompleted() && p.getArrivalTime() <= currentTime) {
                    available.add(p);
                }
            }

            if (available.isEmpty()) {
                int nextArrival = processes.stream()
                        .filter(p -> !p.isCompleted())
                        .mapToInt(Process::getArrivalTime)
                        .min()
                        .orElse(currentTime);
                gantt.add(new GanttSegment(GanttSegment.IDLE, currentTime, nextArrival));
                currentTime = nextArrival;
                continue;
            }

            // Lowest priority number wins; ties broken by arrival then id.
            Process next = available.stream()
                    .min(Comparator.comparingInt(Process::getPriority)
                            .thenComparingInt(Process::getArrivalTime)
                            .thenComparing(Process::getProcessId))
                    .orElseThrow();

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
