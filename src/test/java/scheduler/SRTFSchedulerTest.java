package scheduler;

import model.Process;
import org.junit.jupiter.api.Test;
import result.GanttSegment;
import result.ScheduleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SRTFSchedulerTest {

    private final SRTFScheduler scheduler = new SRTFScheduler();

    @Test
    void singleProcess_behavesLikeFCFS() {
        List<Process> input = List.of(new Process("P1", 0, 6, 0));
        ScheduleResult result = scheduler.schedule(input);
        assertEquals(0, result.getAverageWaitingTime());
        assertEquals(6, result.getProcesses().get(0).getCompletionTime());
    }

    @Test
    void preemptionOccursWhenShorterJobArrives_matchesKnownExample() {
        // Classic Silberschatz SRTF example.
        List<Process> input = List.of(
                new Process("P1", 0, 8, 0),
                new Process("P2", 1, 4, 0),
                new Process("P3", 2, 9, 0),
                new Process("P4", 3, 5, 0)
        );
        ScheduleResult result = scheduler.schedule(input);

        // Known result: Gantt = P1[0-1] P2[1-5] P4[5-10] P1[10-17] P3[17-26]
        List<GanttSegment> gantt = result.getGanttChart();
        assertEquals(5, gantt.size());
        assertEquals("P1", gantt.get(0).getProcessId());
        assertEquals(1, gantt.get(0).getEndTime());
        assertEquals("P2", gantt.get(1).getProcessId());
        assertEquals("P4", gantt.get(2).getProcessId());
        assertEquals("P1", gantt.get(3).getProcessId());
        assertEquals("P3", gantt.get(4).getProcessId());

        assertEquals(6.5, result.getAverageWaitingTime(), 0.001);
    }

    @Test
    void cpuIdlePeriod_beforeFirstArrival() {
        List<Process> input = List.of(new Process("P1", 3, 2, 0));
        ScheduleResult result = scheduler.schedule(input);
        assertEquals("IDLE", result.getGanttChart().get(0).getProcessId());
        assertEquals(0, result.getGanttChart().get(0).getStartTime());
        assertEquals(3, result.getGanttChart().get(0).getEndTime());
    }
}
