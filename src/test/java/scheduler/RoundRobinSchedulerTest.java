package scheduler;

import model.Process;
import org.junit.jupiter.api.Test;
import result.GanttSegment;
import result.ScheduleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundRobinSchedulerTest {

    @Test
    void classicExample_quantumFour_matchesKnownAverageWaitingTime() {
        // Classic Silberschatz Round Robin example, quantum = 4.
        List<Process> input = List.of(
                new Process("P1", 0, 24, 0),
                new Process("P2", 0, 3, 0),
                new Process("P3", 0, 3, 0)
        );
        RoundRobinScheduler scheduler = new RoundRobinScheduler(4);
        ScheduleResult result = scheduler.schedule(input);

        // Known Gantt: P1(0-4) P2(4-7) P3(7-10) P1(10-30)
        List<GanttSegment> gantt = result.getGanttChart();
        assertEquals(4, gantt.size());
        assertEquals("P1", gantt.get(0).getProcessId());
        assertEquals(4, gantt.get(0).getEndTime());
        assertEquals("P2", gantt.get(1).getProcessId());
        assertEquals("P3", gantt.get(2).getProcessId());
        assertEquals("P1", gantt.get(3).getProcessId());
        assertEquals(30, gantt.get(3).getEndTime());

        assertEquals(17.0 / 3.0, result.getAverageWaitingTime(), 0.001);
    }

    @Test
    void quantumLargerThanAllBursts_behavesLikeFCFS() {
        List<Process> input = List.of(
                new Process("P1", 0, 3, 0),
                new Process("P2", 1, 2, 0)
        );
        RoundRobinScheduler scheduler = new RoundRobinScheduler(100);
        ScheduleResult result = scheduler.schedule(input);

        assertEquals(2, result.getGanttChart().size());
        assertEquals("P1", result.getGanttChart().get(0).getProcessId());
        assertEquals(3, result.getGanttChart().get(0).getEndTime());
    }

    @Test
    void newArrivalDuringSlice_isQueuedBeforeReAddingCurrentProcess() {
        // P1 runs first slice 0-2; P2 arrives at t=1 during that slice.
        // Standard convention: P2 should be queued ahead of the re-queued P1.
        List<Process> input = List.of(
                new Process("P1", 0, 4, 0),
                new Process("P2", 1, 2, 0)
        );
        RoundRobinScheduler scheduler = new RoundRobinScheduler(2);
        ScheduleResult result = scheduler.schedule(input);

        List<GanttSegment> gantt = result.getGanttChart();
        assertEquals("P1", gantt.get(0).getProcessId()); // 0-2
        assertEquals("P2", gantt.get(1).getProcessId()); // 2-4 (P2 before P1's remainder)
        assertEquals("P1", gantt.get(2).getProcessId()); // 4-6 (P1 finishes)
    }

    @Test
    void invalidTimeQuantum_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new RoundRobinScheduler(0));
        assertThrows(IllegalArgumentException.class, () -> new RoundRobinScheduler(-1));
    }

    @Test
    void cpuIdlePeriod_isHandled() {
        List<Process> input = List.of(new Process("P1", 5, 3, 0));
        RoundRobinScheduler scheduler = new RoundRobinScheduler(2);
        ScheduleResult result = scheduler.schedule(input);

        Process p1 = result.getProcesses().get(0);
        assertEquals(5, p1.getStartTime());
    }
}
