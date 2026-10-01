package scheduler;

import model.Process;
import result.GanttSegment;
import result.MetricsCalculator;
import result.ScheduleResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Round Robin (RR) - preemptive, time-sliced scheduling.
 *
 * Rule: every process gets the CPU for at most one "time quantum" at a
 * time. If it doesn't finish in that slice, it goes to the BACK of the
 * ready queue and waits its turn again. This is what makes RR fair and
 * responsive - no process waits forever behind a long one.
 *
 * Convention followed here (the standard textbook one): when a time slice
 * ends, any processes that arrived DURING that slice are added to the
 * ready queue BEFORE the just-run process is re-added (if it isn't finished).
 */
public class RoundRobinScheduler implements SchedulingAlgorithm {

    private final int timeQuantum;

    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be greater than 0.");
        }
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String getName() {
        return "Round Robin (RR, quantum=" + timeQuantum + ")";
    }

    @Override
    public ScheduleResult schedule(List<Process> inputProcesses) {
        List<Process> processes = new ArrayList<>();
        for (Process p : inputProcesses) {
            processes.add(p.copyForSimulation());
        }

        // Sorted view used only to discover arrivals in order, one by one.
        List<Process> byArrival = new ArrayList<>(processes);
        byArrival.sort(Comparator.comparingInt(Process::getArrivalTime)
                .thenComparing(Process::getProcessId));

        Deque<Process> readyQueue = new ArrayDeque<>();
        List<GanttSegment> gantt = new ArrayList<>();
        int arrivalPointer = 0;
        int n = byArrival.size();
        int currentTime = 0;

        // Step 1: if nobody has arrived at time 0, fast-forward to the first arrival.
        if (byArrival.get(0).getArrivalTime() > 0) {
            currentTime = byArrival.get(0).getArrivalTime();
        }

        // Step 2: load every process that has arrived by currentTime into the queue.
        while (arrivalPointer < n && byArrival.get(arrivalPointer).getArrivalTime() <= currentTime) {
            readyQueue.add(byArrival.get(arrivalPointer));
            arrivalPointer++;
        }

        while (!readyQueue.isEmpty()) {
            Process current = readyQueue.poll();

            // Record response time only the first time a process ever runs.
            if (!current.isStarted()) {
                current.setStartTime(currentTime);
                current.setResponseTime(currentTime - current.getArrivalTime());
                current.setStarted(true);
            }

            // Step 3: run for min(quantum, remaining work).
            int sliceLength = Math.min(timeQuantum, current.getRemainingBurstTime());
            int sliceStart = currentTime;
            int sliceEnd = currentTime + sliceLength;

            addOrExtendSegment(gantt, current.getProcessId(), sliceStart, sliceEnd);
            current.setRemainingBurstTime(current.getRemainingBurstTime() - sliceLength);
            currentTime = sliceEnd;

            // Step 4: enqueue every process that arrived DURING this slice,
            // in arrival order, before deciding whether to re-queue "current".
            while (arrivalPointer < n && byArrival.get(arrivalPointer).getArrivalTime() <= currentTime) {
                readyQueue.add(byArrival.get(arrivalPointer));
                arrivalPointer++;
            }

            if (current.isCompleted()) {
                current.setCompletionTime(currentTime);
                current.setTurnaroundTime(currentTime - current.getArrivalTime());
                current.setWaitingTime(current.getTurnaroundTime() - current.getBurstTime());
            } else {
                // Step 5: unfinished - goes to the back of the line.
                readyQueue.add(current);
            }

            // Step 6: if the queue just went empty but processes are still
            // waiting to arrive, the CPU is idle - jump forward to that arrival.
            if (readyQueue.isEmpty() && arrivalPointer < n) {
                int nextArrival = byArrival.get(arrivalPointer).getArrivalTime();
                if (nextArrival > currentTime) {
                    addOrExtendSegment(gantt, GanttSegment.IDLE, currentTime, nextArrival);
                    currentTime = nextArrival;
                }
                while (arrivalPointer < n && byArrival.get(arrivalPointer).getArrivalTime() <= currentTime) {
                    readyQueue.add(byArrival.get(arrivalPointer));
                    arrivalPointer++;
                }
            }
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

    /**
     * Adds a new Gantt segment, unless the same process just ran and this
     * new slice starts exactly where the last one ended - in that case we
     * extend the existing block instead of creating a visually-identical
     * neighbor. This happens whenever a process gets back-to-back turns
     * because it's the only one left in the ready queue (e.g. one long
     * process running alone after the others finish).
     */
    private void addOrExtendSegment(List<GanttSegment> gantt, String processId, int start, int end) {
        if (!gantt.isEmpty()) {
            GanttSegment last = gantt.get(gantt.size() - 1);
            if (last.getProcessId().equals(processId) && last.getEndTime() == start) {
                gantt.set(gantt.size() - 1, new GanttSegment(processId, last.getStartTime(), end));
                return;
            }
        }
        gantt.add(new GanttSegment(processId, start, end));
    }
}
