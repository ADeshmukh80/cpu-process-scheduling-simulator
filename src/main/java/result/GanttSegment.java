package result;

/**
 * Represents one contiguous block of CPU execution in the Gantt chart.
 * Example: GanttSegment("P1", 0, 5) means P1 ran on the CPU from time 0 to 5.
 * "IDLE" is used as the processId when the CPU had nothing to run.
 */
public class GanttSegment {

    public static final String IDLE = "IDLE";

    private final String processId;
    private final int startTime;
    private final int endTime;

    public GanttSegment(String processId, int startTime, int endTime) {
        this.processId = processId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getProcessId() {
        return processId;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return endTime - startTime;
    }

    @Override
    public String toString() {
        return processId + "[" + startTime + "-" + endTime + "]";
    }
}
