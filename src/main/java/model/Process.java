package model;

/**
 * Represents a single process in the CPU scheduling simulation.
 * This is a simplified version of a Process Control Block (PCB).
 *
 * Input fields (given by the user): processId, arrivalTime, burstTime, priority
 * Computed fields (filled in after a scheduling algorithm runs): startTime,
 * completionTime, turnaroundTime, waitingTime, responseTime.
 */
public class Process {

    // ----- Input fields -----
    private final String processId;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;

    // ----- Computed fields (set by schedulers) -----
    private int startTime;
    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;
    private int responseTime;

    // Used during simulation to track how much burst time is left
    // (needed by SRTF, preemptive priority, and Round Robin).
    private int remainingBurstTime;

    // Tracks whether this process has been given the CPU at least once yet.
    // Needed to correctly compute responseTime only on the FIRST execution.
    private boolean started;

    public Process(String processId, int arrivalTime, int burstTime, int priority) {
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingBurstTime = burstTime;
        this.started = false;
    }

    /**
     * Creates a fresh, independent copy of this process with all computed
     * fields reset. Used so the same input list can be simulated by multiple
     * algorithms without one run's results leaking into another's.
     */
    public Process copyForSimulation() {
        return new Process(this.processId, this.arrivalTime, this.burstTime, this.priority);
    }

    // ----- Getters for input fields -----
    public String getProcessId() {
        return processId;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    // ----- Getters/setters for computed fields -----
    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public int getRemainingBurstTime() {
        return remainingBurstTime;
    }

    public void setRemainingBurstTime(int remainingBurstTime) {
        this.remainingBurstTime = remainingBurstTime;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isCompleted() {
        return remainingBurstTime <= 0;
    }

    @Override
    public String toString() {
        return "Process{" +
                "id='" + processId + '\'' +
                ", arrival=" + arrivalTime +
                ", burst=" + burstTime +
                ", priority=" + priority +
                '}';
    }
}
