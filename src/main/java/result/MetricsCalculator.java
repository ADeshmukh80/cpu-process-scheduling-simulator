package result;

import model.Process;

import java.util.List;

/**
 * Small shared utility so every scheduler computes averages the same way,
 * instead of repeating this math in five different classes.
 */
public final class MetricsCalculator {

    private MetricsCalculator() {
        // utility class - not meant to be instantiated
    }

    public static double averageWaitingTime(List<Process> processes) {
        return processes.stream().mapToInt(Process::getWaitingTime).average().orElse(0.0);
    }

    public static double averageTurnaroundTime(List<Process> processes) {
        return processes.stream().mapToInt(Process::getTurnaroundTime).average().orElse(0.0);
    }

    public static double averageResponseTime(List<Process> processes) {
        return processes.stream().mapToInt(Process::getResponseTime).average().orElse(0.0);
    }
}
