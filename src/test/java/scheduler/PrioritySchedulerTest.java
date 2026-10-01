package scheduler;

import model.Process;
import org.junit.jupiter.api.Test;
import result.ScheduleResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrioritySchedulerTest {

    private final PrioritySchedulerNonPreemptive nonPreemptive = new PrioritySchedulerNonPreemptive();
    private final PrioritySchedulerPreemptive preemptive = new PrioritySchedulerPreemptive();

    @Test
    void nonPreemptive_classicExample_matchesKnownAverageWaitingTime() {
        // Classic Silberschatz priority scheduling example (lower number = higher priority).
        List<Process> input = List.of(
                new Process("P1", 0, 10, 3),
                new Process("P2", 0, 1, 1),
                new Process("P3", 0, 2, 4),
                new Process("P4", 0, 1, 5),
                new Process("P5", 0, 5, 2)
        );
        ScheduleResult result = nonPreemptive.schedule(input);

        // Known Gantt order: P2, P5, P1, P3, P4
        assertEquals("P2", result.getGanttChart().get(0).getProcessId());
        assertEquals("P5", result.getGanttChart().get(1).getProcessId());
        assertEquals("P1", result.getGanttChart().get(2).getProcessId());
        assertEquals("P3", result.getGanttChart().get(3).getProcessId());
        assertEquals("P4", result.getGanttChart().get(4).getProcessId());

        assertEquals(8.2, result.getAverageWaitingTime(), 0.001);
    }

    @Test
    void nonPreemptive_priorityTie_brokenByArrivalThenId() {
        List<Process> input = List.of(
                new Process("P2", 0, 3, 1),
                new Process("P1", 0, 3, 1)
        );
        ScheduleResult result = nonPreemptive.schedule(input);
        assertEquals("P1", result.getGanttChart().get(0).getProcessId());
    }

    @Test
    void preemptive_higherPriorityArrivalPreemptsRunningProcess() {
        // P1 (priority 2) starts first; P2 (priority 1, better) arrives at t=1 and should preempt it.
        List<Process> input = List.of(
                new Process("P1", 0, 5, 2),
                new Process("P2", 1, 2, 1)
        );
        ScheduleResult result = preemptive.schedule(input);

        assertEquals("P1", result.getGanttChart().get(0).getProcessId());
        assertEquals(1, result.getGanttChart().get(0).getEndTime());
        assertEquals("P2", result.getGanttChart().get(1).getProcessId());
    }

    @Test
    void singleProcess_noPreemptionNeeded() {
        List<Process> input = List.of(new Process("P1", 0, 4, 1));
        ScheduleResult result = preemptive.schedule(input);
        assertEquals(0, result.getAverageWaitingTime());
    }
}
