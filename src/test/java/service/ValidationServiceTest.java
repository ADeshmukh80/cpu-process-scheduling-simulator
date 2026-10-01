package service;

import model.Process;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationServiceTest {

    private final ValidationService validationService = new ValidationService();

    @Test
    void emptyList_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateProcessList(List.of()));
    }

    @Test
    void negativeArrivalTime_throwsException() {
        List<Process> processes = List.of(new Process("P1", -1, 5, 0));
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateProcessList(processes));
    }

    @Test
    void zeroBurstTime_throwsException() {
        List<Process> processes = List.of(new Process("P1", 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateProcessList(processes));
    }

    @Test
    void negativeBurstTime_throwsException() {
        List<Process> processes = List.of(new Process("P1", 0, -3, 0));
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateProcessList(processes));
    }

    @Test
    void duplicateProcessId_throwsException() {
        List<Process> processes = List.of(
                new Process("P1", 0, 3, 0),
                new Process("P1", 1, 2, 0)
        );
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateProcessList(processes));
    }

    @Test
    void negativePriority_throwsException() {
        List<Process> processes = List.of(new Process("P1", 0, 3, -1));
        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateProcessList(processes));
    }

    @Test
    void validList_doesNotThrow() {
        List<Process> processes = List.of(
                new Process("P1", 0, 3, 1),
                new Process("P2", 1, 2, 2)
        );
        validationService.validateProcessList(processes); // should not throw
    }

    @Test
    void invalidTimeQuantum_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> validationService.validateTimeQuantum(0));
        assertThrows(IllegalArgumentException.class, () -> validationService.validateTimeQuantum(-5));
    }
}
