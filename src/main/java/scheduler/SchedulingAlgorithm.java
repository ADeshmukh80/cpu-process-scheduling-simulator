package scheduler;

import model.Process;
import result.ScheduleResult;

import java.util.List;

/**
 * Common contract every CPU scheduling algorithm must follow.
 * This is what lets the rest of the application (GUI, comparison logic)
 * treat FCFS, SJF, SRTF, Priority, and Round Robin interchangeably -
 * classic "program to an interface, not an implementation".
 */
public interface SchedulingAlgorithm {

    /**
     * Runs the algorithm on the given processes and returns the full result:
     * per-process metrics, Gantt chart segments, and averages.
     *
     * Implementations must NOT mutate the input list's Process objects;
     * they should work on copies (see Process#copyForSimulation).
     */
    ScheduleResult schedule(List<Process> processes);

    /**
     * Human-readable algorithm name, e.g. "First Come First Serve (FCFS)".
     * Used in the GUI dropdown and comparison table.
     */
    String getName();
}
