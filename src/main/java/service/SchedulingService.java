package service;

import model.Process;
import result.ScheduleResult;
import scheduler.FCFSScheduler;
import scheduler.PrioritySchedulerNonPreemptive;
import scheduler.PrioritySchedulerPreemptive;
import scheduler.RoundRobinScheduler;
import scheduler.SJFScheduler;
import scheduler.SRTFScheduler;
import scheduler.SchedulingAlgorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * The single entry point the GUI (or anything else) talks to. It hides two
 * details from the caller: (1) input validation, and (2) picking the right
 * SchedulingAlgorithm implementation for a given algorithm name.
 *
 * This is the "service layer" from the architecture in Module 11 - it sits
 * between the UI and the scheduler/model classes.
 */
public class SchedulingService {

    public enum AlgorithmType {
        FCFS,
        SJF,
        SRTF,
        PRIORITY_NON_PREEMPTIVE,
        PRIORITY_PREEMPTIVE,
        ROUND_ROBIN
    }

    private final ValidationService validationService = new ValidationService();

    public ScheduleResult run(List<Process> processes, AlgorithmType type, int timeQuantum) {
        validationService.validateProcessList(processes);

        SchedulingAlgorithm algorithm = createAlgorithm(type, timeQuantum);
        return algorithm.schedule(processes);
    }

    /**
     * Runs every algorithm on the same process set so results can be
     * displayed side by side (Module 10 - Algorithm Comparison).
     */
    public List<ScheduleResult> runAllForComparison(List<Process> processes, int timeQuantum) {
        validationService.validateProcessList(processes);

        List<ScheduleResult> results = new ArrayList<>();
        for (AlgorithmType type : AlgorithmType.values()) {
            SchedulingAlgorithm algorithm = createAlgorithm(type, timeQuantum);
            results.add(algorithm.schedule(processes));
        }
        return results;
    }

    private SchedulingAlgorithm createAlgorithm(AlgorithmType type, int timeQuantum) {
        switch (type) {
            case FCFS:
                return new FCFSScheduler();
            case SJF:
                return new SJFScheduler();
            case SRTF:
                return new SRTFScheduler();
            case PRIORITY_NON_PREEMPTIVE:
                return new PrioritySchedulerNonPreemptive();
            case PRIORITY_PREEMPTIVE:
                return new PrioritySchedulerPreemptive();
            case ROUND_ROBIN:
                validationService.validateTimeQuantum(timeQuantum);
                return new RoundRobinScheduler(timeQuantum);
            default:
                throw new IllegalArgumentException("Unknown algorithm type: " + type);
        }
    }
}
