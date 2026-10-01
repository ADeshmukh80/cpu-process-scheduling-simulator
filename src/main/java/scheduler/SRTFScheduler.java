package scheduler;

import model.Process;
import result.GanttSegment;
import result.MetricsCalculator;
import result.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shortest Remaining Time First (SRTF) - the preemptive version of SJF.
 *
 * Rule: at every moment in time, the CPU runs whichever arrived, unfinished
 * process currently has the SMALLEST remaining burst time. If a new process
 * arrives with less remaining work than what's currently running, the
 * running process is PREEMPTED (paused) and the new one takes over.
 *
 * Implementation approach: simulate time one unit at a time. This is easy
 * to reason about and explain (great for interviews), and is fast enough
 * for the small process counts this educational tool is meant for.
 */
public class SRTFScheduler implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "Shortest Remaining Time First (SRTF)";
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

        // Tracks the process id currently occupying the "open" gantt segment,
        // so consecutive time units on the same process merge into one block
        // instead of creating one segment per unit.
        String openSegmentProcessId = null;
        int openSegmentStart = 0;

        while (completedCount < n) {
            // Step 1: among arrived, unfinished processes, pick smallest remaining time.
            final int currentTimeSnapshot = currentTime;
            Process chosen = processes.stream()
                    .filter(p -> !p.isCompleted() && p.getArrivalTime() <= currentTimeSnapshot)
                    .min(Comparator.comparingInt(Process::getRemainingBurstTime)
                            .thenComparingInt(Process::getArrivalTime)
                            .thenComparing(Process::getProcessId))
                    .orElse(null);

            if (chosen == null) {
                // CPU idle for this unit.
                if (!GanttSegment.IDLE.equals(openSegmentProcessId)) {
                    closeSegment(gantt, openSegmentProcessId, openSegmentStart, currentTime);
                    openSegmentProcessId = GanttSegment.IDLE;
                    openSegmentStart = currentTime;
                }
                currentTime++;
                continue;
            }

            // Step 2: record response time the FIRST time a process ever runs.
            if (!chosen.isStarted()) {
                chosen.setStartTime(currentTime);
                chosen.setResponseTime(currentTime - chosen.getArrivalTime());
                chosen.setStarted(true);
            }

            // Step 3: if the CPU is switching to a different process, close the
            // previous open segment and start a new one (this IS the preemption
            // becoming visible on the Gantt chart).
            if (!chosen.getProcessId().equals(openSegmentProcessId)) {
                closeSegment(gantt, openSegmentProcessId, openSegmentStart, currentTime);
                openSegmentProcessId = chosen.getProcessId();
                openSegmentStart = currentTime;
            }

            // Step 4: execute exactly one time unit of this process.
            chosen.setRemainingBurstTime(chosen.getRemainingBurstTime() - 1);
            currentTime++;

            // Step 5: if it just finished, compute its final metrics.
            if (chosen.isCompleted()) {
                chosen.setCompletionTime(currentTime);
                chosen.setTurnaroundTime(currentTime - chosen.getArrivalTime());
                chosen.setWaitingTime(chosen.getTurnaroundTime() - chosen.getBurstTime());
                completedCount++;
            }
        }

        // Close whatever segment was left open at the very end.
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
