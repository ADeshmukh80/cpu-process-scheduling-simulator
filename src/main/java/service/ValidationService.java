package service;

import model.Process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks that user input makes sense before we ever try to schedule it.
 * All failures throw IllegalArgumentException with a message written for
 * an end user (not a stack trace) - the GUI shows this message directly
 * in an alert dialog.
 */
public class ValidationService {

    public void validateProcessList(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("Please add at least one process before running the simulation.");
        }

        Set<String> seenIds = new HashSet<>();

        for (Process p : processes) {
            if (p.getProcessId() == null || p.getProcessId().isBlank()) {
                throw new IllegalArgumentException("Every process must have a non-empty Process ID.");
            }

            if (!seenIds.add(p.getProcessId())) {
                throw new IllegalArgumentException(
                        "Duplicate Process ID found: \"" + p.getProcessId() + "\". Each process needs a unique ID.");
            }

            if (p.getArrivalTime() < 0) {
                throw new IllegalArgumentException(
                        "Process " + p.getProcessId() + " has a negative arrival time. Arrival time cannot be negative.");
            }

            if (p.getBurstTime() <= 0) {
                throw new IllegalArgumentException(
                        "Process " + p.getProcessId() + " has an invalid burst time. Burst time must be greater than 0.");
            }

            if (p.getPriority() < 0) {
                throw new IllegalArgumentException(
                        "Process " + p.getProcessId() + " has an invalid priority. Priority cannot be negative.");
            }
        }
    }

    public void validateTimeQuantum(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be a positive number (greater than 0).");
        }
    }
}
