package scheduler;

import model.Process;
import org.junit.jupiter.api.Test;
import result.ScheduleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SJFSchedulerTest {

    private final SJFScheduler scheduler = new SJFScheduler();

    @Test
    void singleProcess() {
        List<Process> input = List.of(new Process("P1", 0, 4, 0));
        ScheduleResult result = scheduler.schedule(input);
        assertEquals(0, result.getAverageWaitingTime());
    }

    @Test
    void classicExample_matchesKnownAverageWaitingTime() {
        List<Process> input = List.of(
                new Process("P1", 0, 8, 0),
                new Process("P2", 1, 4, 0),
                new Process("P3", 2, 9, 0),
                new Process("P4", 3, 5, 0)
        );
        ScheduleResult result = scheduler.schedule(input);

        // Hand-verified: Gantt order is P1(0-8), P2(8-12), P4(12-17), P3(17-26)
        assertEquals(7.75, result.getAverageWaitingTime(), 0.001);
    }

    @Test
    void sameBurstTime_tieBrokenByArrivalThenId() {
        List<Process> input = List.of(
                new Process("P2", 0, 3, 0),
                new Process("P1", 0, 3, 0)
        );
        ScheduleResult result = scheduler.schedule(input);
        assertEquals("P1", result.getGanttChart().get(0).getProcessId());
    }

    @Test
    void cpuIdleUntilShortestJobArrives() {
        List<Process> input = List.of(new Process("P1", 5, 2, 0));
        ScheduleResult result = scheduler.schedule(input);

        assertEquals("IDLE", result.getGanttChart().get(0).getProcessId());
        assertEquals(5, result.getGanttChart().get(0).getEndTime());
    }
}
