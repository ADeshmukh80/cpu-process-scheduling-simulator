package scheduler;

import model.Process;
import result.GanttSegment;
import result.MetricsCalculator;
import result.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Priority Scheduling - preemptive.
 *
 * Same convention as the non-preemptive version: LOWER priority number
 * means HIGHER priority. The difference is that if a newly arrived process
 * has better (lower) priority than the one currently running, the CPU is
 * taken away immediately and given to the new arrival - just like SRTF
 * preempts on remaining time instead of priority.
 *
 * Implementation mirrors SRTFScheduler's unit-by-unit approach.
 */
public class PrioritySchedulerPreemptive implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "Priority Scheduling (Preemptive)";
    }

    @Override
    public ScheduleResult schedule(List<Process> inputProcesses) {
        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(p.copyForSimulation());
        }

        int n = processes.size();
        int completedCount = 0;
        int currentTime = 0;
        List<GanttSegment> gantt = new ArrayList<>();

        String openSegmentProcessId = null;
        int openSegmentStart = 0;

        while (completedCount < n) {
            final int currentTimeSnapshot = currentTime;
            Process chosen = processes.stream()
                    .filter(p -> !p.isCompleted() && p.getArrivalTime() <= currentTimeSnapshot)
                    .min(Comparator.comparingInt(Process::getPriority)
                            .thenComparingInt(Process::getArrivalTime)
                            .thenComparing(Process::getProcessId))
                    .orElse(null);

            if (chosen == null) {
                if (!GanttSegment.IDLE.equals(openSegmentProcessId)) {
                    closeSegment(gantt, openSegmentProcessId, openSegmentStart, currentTime);
                    openSegmentProcessId = GanttSegment.IDLE;
                    openSegmentStart = currentTime;
                }
                currentTime++;
                continue;
            }

            if (!chosen.isStarted()) {
                chosen.setStartTime(currentTime);
                chosen.setResponseTime(currentTime - chosen.getArrivalTime());
                chosen.setStarted(true);
            }

            if (!chosen.getProcessId().equals(openSegmentProcessId)) {
                closeSegment(gantt, openSegmentProcessId, openSegmentStart, currentTime);
                openSegmentProcessId = chosen.getProcessId();
                openSegmentStart = currentTime;
            }

            chosen.setRemainingBurstTime(chosen.getRemainingBurstTime() - 1);
            currentTime++;

            if (chosen.isCompleted()) {
                chosen.setCompletionTime(currentTime);
                chosen.setTurnaroundTime(currentTime - chosen.getArrivalTime());
                chosen.setWaitingTime(chosen.getTurnaroundTime() - chosen.getBurstTime());
                completedCount++;
            }
        }

        closeSegment(gantt, openSegmentProcessId, openSegmentStart, currentTime);

        return new ScheduleResult(
                getName(),
                processes,
                gantt,
                MetricsCalculator.averageWaitingTime(processes),
                MetricsCalculator.averageTurnaroundTime(processes),
                MetricsCalculator.averageResponseTime(processes)
        );
    }

    private void closeSegment(List<GanttSegment> gantt, String processId, int start, int end) {
        if (processId != null && end > start) {
            gantt.add(new GanttSegment(processId, start, end));
        }
    }
}
