package scheduler;

import model.Process;
import result.GanttSegment;
import result.MetricsCalculator;
import result.ScheduleResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * First Come First Serve (FCFS) - non-preemptive.
 *
 * Rule: whichever process arrives earliest runs first, and once it starts
 * it runs to completion with no interruption. Ties in arrival time are
 * broken by process ID so results are deterministic and repeatable.
 */
public class FCFSScheduler implements SchedulingAlgorithm {

    @Override
    public String getName() {
        return "First Come First Serve (FCFS)";
    }

    @Override
    public ScheduleResult schedule(List<Process> inputProcesses) {
        // Work on fresh copies so we never mutate the caller's objects.
        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(p.copyForSimulation());
        }

        // Step 1: sort strictly by arrival time (tie-break by processId).
        processes.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getProcessId));

        List<GanttSegment> gantt = new ArrayList<>();
        int currentTime = 0;

        for (Process p : processes) {
            // Step 2: if the CPU would be idle waiting for this process, jump forward.
            if (currentTime < p.getArrivalTime()) {
                gantt.add(new GanttSegment(GanttSegment.IDLE, currentTime, p.getArrivalTime()));
                currentTime = p.getArrivalTime();
            }

            // Step 3: run the process fully, from currentTime to currentTime+burst.
            int start = currentTime;
            int completion = start + p.getBurstTime();

            p.setStartTime(start);
            p.setCompletionTime(completion);
            p.setResponseTime(start - p.getArrivalTime());
            p.setTurnaroundTime(completion - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            p.setRemainingBurstTime(0);

            gantt.add(new GanttSegment(p.getProcessId(), start, completion));
            currentTime = completion;
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
