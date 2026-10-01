# CPU Process Scheduling Simulator

An educational Java/JavaFX application that simulates how an Operating
System schedules processes on the CPU, visualizes execution with a Gantt
chart, and compares scheduling algorithms using standard performance
metrics.

## Problem Statement

An Operating System must decide, at every moment, which of the many
processes waiting for the CPU gets to run next. This decision is made by
a **CPU scheduling algorithm**. Different algorithms make this decision
differently, and each comes with trade-offs in fairness, responsiveness,
and throughput. This project lets you enter a set of processes, run them
through five classic scheduling algorithms, and directly compare the
results.

## Objectives

- Model a process the way an OS conceptually does (a simplified PCB).
- Implement FCFS, SJF, SRTF, Priority (preemptive & non-preemptive), and
  Round Robin scheduling.
- Compute standard metrics: completion time, turnaround time, waiting
  time, and response time.
- Visualize execution order on a Gantt chart.
- Let a user compare all algorithms on the same workload side by side.

## Features

- Add / remove / clear processes through a JavaFX GUI.
- Select an algorithm from a dropdown and run the simulation.
- Interactive Gantt chart (color-coded per process, with tooltips).
- Per-process results table (AT, BT, Priority, CT, TAT, WT, RT).
- Average waiting / turnaround / response time.
- "Compare All Algorithms" view showing every algorithm's averages
  side by side.
- Input validation with friendly error messages.

## Technologies

- Java 17+
- JavaFX (GUI)
- Maven (build tool)
- JUnit 5 (unit testing)
- Java Collections Framework (List, Deque/ArrayDeque, Comparator)

## OS Concepts Used

CPU scheduling, ready queue, dispatcher, preemptive vs. non-preemptive
scheduling, context switching, CPU idle time, turnaround time, waiting
time, response time, starvation, and time-slicing (quantum).

## Algorithms Implemented

| Algorithm | Preemptive? | Selection rule |
|---|---|---|
| FCFS | No | Whoever arrives first runs first |
| SJF | No | Shortest burst time among arrived processes |
| SRTF | Yes | Shortest *remaining* burst time at every instant |
| Priority (Non-Preemptive) | No | Lowest priority number among arrived processes (lower number = higher priority) |
| Priority (Preemptive) | Yes | Same rule, but re-evaluated at every instant |
| Round Robin | Yes | Fixed time slice (quantum) per turn, cycling through a ready queue |

## Architecture

```
model/       Process.java                     - the core data entity
result/      GanttSegment.java                 - one block of the Gantt chart
             ScheduleResult.java               - full output of one algorithm run
             MetricsCalculator.java            - shared average calculations
scheduler/   SchedulingAlgorithm.java          - common interface
             FCFSScheduler.java
             SJFScheduler.java
             SRTFScheduler.java
             PrioritySchedulerNonPreemptive.java
             PrioritySchedulerPreemptive.java
             RoundRobinScheduler.java
service/     ValidationService.java            - input validation
             SchedulingService.java            - orchestrates validation + scheduling
ui/          MainApp.java                      - JavaFX GUI (entry point)
```

Every scheduler implements the same `SchedulingAlgorithm` interface, so
the GUI and the comparison feature can treat all six algorithms
interchangeably - this is basic polymorphism / "program to an interface".

## How to Run

### Prerequisites
- JDK 17 or later installed
- Maven installed (or use your IDE's built-in Maven support)

### Run the GUI
```bash
mvn clean javafx:run
```

### Run the tests
```bash
mvn test
```

### Build a runnable jar
```bash
mvn clean package
```

## Sample Input

| Process | Arrival Time | Burst Time | Priority |
|---|---|---|---|
| P1 | 0 | 8 | 3 |
| P2 | 1 | 4 | 1 |
| P3 | 2 | 9 | 4 |
| P4 | 3 | 5 | 2 |

## Sample Output (SJF, Non-Preemptive)

Gantt Chart: `P1[0-8] | P2[8-12] | P4[12-17] | P3[17-26]`

| Process | AT | BT | CT | TAT | WT | RT |
|---|---|---|---|---|---|---|
| P1 | 0 | 8 | 8 | 8 | 0 | 0 |
| P2 | 1 | 4 | 12 | 11 | 7 | 7 |
| P4 | 3 | 5 | 17 | 14 | 9 | 9 |
| P3 | 2 | 9 | 26 | 24 | 15 | 15 |

Average Waiting Time: **7.75**
Average Turnaround Time: **14.25**

## Test Cases Covered

Each algorithm has JUnit tests for: a single process, multiple processes
with different arrival times, processes sharing the same arrival time
(tie-breaking), processes sharing the same burst time, a CPU idle period
before the first arrival, priority ties, preemption (SRTF and preemptive
Priority), and Round Robin with different quantum values (including a
quantum larger than every burst time, which should behave like FCFS).
Input validation is tested separately (empty list, negative arrival time,
zero/negative burst time, duplicate IDs, negative priority, invalid
quantum).

## Future Improvements

- Multilevel Queue and Multilevel Feedback Queue scheduling
- Persisting process sets to a file and reloading them
- Exporting the Gantt chart / results table as an image or PDF
- Support for I/O burst simulation (not just CPU bursts)
- Animated, step-by-step Gantt chart playback

## License

Educational project - free to use and adapt for learning purposes.
