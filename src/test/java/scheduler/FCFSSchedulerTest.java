package scheduler;

import model.Process;
import org.junit.jupiter.api.Test;
import result.ScheduleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FCFSSchedulerTest {

    private final FCFSScheduler scheduler = new FCFSScheduler();

    @Test
    void singleProcess_completesImmediately() {
        List<Process> input = List.of(new Process("P1", 0, 5, 0));
        ScheduleResult result = scheduler.schedule(input);

        Process p1 = result.getProcesses().get(0);
        assertEquals(0, p1.getStartTime());
        assertEquals(5, p1.getCompletionTime());
        assertEquals(0, p1.getWaitingTime());
        assertEquals(0, p1.getResponseTime());
    }

    @Test
    void multipleProcesses_differentArrivalTimes_matchesKnownExample() {
        // Classic textbook example.
        List<Process> input = List.of(
                new Process("P1", 0, 5, 0),
                new Process("P2", 1, 3, 0),
                new Process("P3", 2, 8, 0),
                new Process("P4", 3, 6, 0)
        );
        ScheduleResult result = scheduler.schedule(input);

        assertEquals(5.75, result.getAverageWaitingTime(), 0.001);
        assertEquals(11.25, result.getAverageTurnaroundTime(), 0.001);
    }

    @Test
    void cpuIdlePeriod_isHandledCorrectly() {
        // P1 doesn't arrive until t=2, so the CPU must idle from 0 to 2.
        List<Process> input = List.of(new Process("P1", 2, 3, 0));
        ScheduleResult result = scheduler.schedule(input);

        Process p1 = result.getProcesses().get(0);
        assertEquals(2, p1.getStartTime());
        assertEquals(5, p1.getCompletionTime());

        assertEquals(2, result.getGanttChart().size()); // one IDLE segment + one P1 segment
        assertEquals("IDLE", result.getGanttChart().get(0).getProcessId());
        assertEquals(0, result.getGanttChart().get(0).getStartTime());
        assertEquals(2, result.getGanttChart().get(0).getEndTime());
    }

    @Test
    void sameArrivalTime_tieBrokenByProcessId() {
        List<Process> input = List.of(
                new Process("P2", 0, 3, 0),
                new Process("P1", 0, 2, 0)
        );
        ScheduleResult result = scheduler.schedule(input);

        // P1 should run first because of the ID tie-break, even though it was added second.
        assertEquals("P1", result.getGanttChart().get(0).getProcessId());
    }
}
