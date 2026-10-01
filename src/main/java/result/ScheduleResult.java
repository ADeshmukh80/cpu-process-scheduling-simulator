package result;

import model.Process;

import java.util.Collections;
import java.util.List;

/**
 * Holds everything produced by running one scheduling algorithm on one
 * set of processes: the per-process metrics, the Gantt chart data, and
 * the averages used to judge performance.
 */
public class ScheduleResult {

    private final String algorithmName;
    private final List<Process> processes;
    private final List<GanttSegment> ganttChart;
    private final double averageWaitingTime;
    private final double averageTurnaroundTime;
    private final double averageResponseTime;

    public ScheduleResult(String algorithmName,
                           List<Process> processes,
                           List<GanttSegment> ganttChart,
                           double averageWaitingTime,
                           double averageTurnaroundTime,
                           double averageResponseTime) {
        this.algorithmName = algorithmName;
        this.processes = Collections.unmodifiableList(processes);
        this.ganttChart = Collections.unmodifiableList(ganttChart);
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
        this.averageResponseTime = averageResponseTime;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public List<GanttSegment> getGanttChart() {
        return ganttChart;
    }

    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getAverageTurnaroundTime() {
        return averageTurnaroundTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }
}
