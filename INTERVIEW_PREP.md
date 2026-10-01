# Interview Preparation - CPU Process Scheduling Simulator

## 30-Second Explanation
"I built a Java and JavaFX application that simulates how an Operating
System schedules processes on the CPU. Users enter processes with an
arrival time, burst time, and priority, pick one of five scheduling
algorithms - FCFS, SJF, SRTF, Priority, or Round Robin - and the app
computes waiting time, turnaround time, and response time, and displays
a Gantt chart. It also lets you compare all algorithms on the same
workload side by side."

## 2-Minute Explanation
"CPU scheduling is one of the core topics in Operating Systems, so I
wanted a hands-on project that made the algorithms concrete instead of
just theoretical. I modeled a Process class similar to a simplified
Process Control Block - it holds arrival time, burst time, priority, and
the computed metrics once scheduling runs.

I implemented six scheduling strategies behind one common
`SchedulingAlgorithm` interface: FCFS and non-preemptive SJF run
processes to completion once chosen; SRTF and preemptive Priority
re-evaluate every time unit and can interrupt a running process; Round
Robin uses a queue and a fixed time quantum so no process waits forever.
Because they all implement the same interface, the rest of the app -
the GUI, the validation layer, and the comparison feature - doesn't care
which algorithm is running.

I validated every algorithm against known textbook examples (Silberschatz's
OS book) by hand-tracing the expected Gantt chart and average waiting
time, then wrote JUnit tests asserting those exact numbers, so I have
confidence the logic is correct, not just that it runs without crashing.

The GUI is built in JavaFX: you add processes, pick an algorithm, run
the simulation, and see a color-coded Gantt chart plus a results table
with per-process and average metrics. There's also a 'Compare All'
button that runs every algorithm on the same input and shows their
average waiting/turnaround/response times side by side, which is a nice
way to visually demonstrate that no algorithm is universally best -
each optimizes a different goal."

## Architecture Explanation
Five packages, each with one job: `model` holds the Process entity;
`result` holds the output shapes (GanttSegment, ScheduleResult); the
`scheduler` package holds one class per algorithm, all implementing
`SchedulingAlgorithm`; `service` sits between the GUI and the schedulers,
handling validation and picking the right algorithm; `ui` is the JavaFX
front end. This separation means I could swap the GUI for a command-line
interface without touching a single scheduling algorithm.

## Why Java?
Strong typing catches mistakes at compile time, the Collections
Framework (List, Deque, Comparator) maps naturally onto queues and
sorting, and JavaFX gives a real desktop GUI without needing a separate
front-end stack - all useful for an OS-concepts project meant to be
read and understood, not just run.

## Why These Algorithms?
FCFS and SJF are the simplest non-preemptive baselines. SRTF shows what
preemption buys you (shorter average waiting time) at the cost of more
context switches. Priority scheduling shows how you can optimize for
importance rather than duration - and its preemptive variant shows
starvation risk. Round Robin shows how time-slicing trades some
throughput for fairness and responsiveness. Together they cover the
main strategies taught in every OS course.

## FCFS vs SJF
FCFS ignores burst time entirely - whoever arrives first runs first,
which is simple but can cause the "convoy effect" (a long process makes
everyone behind it wait). SJF picks whichever *available* process has
the smallest burst time, which minimizes average waiting time but
requires knowing burst times in advance and can starve long processes.

## SJF vs SRTF
SJF is non-preemptive: once a process starts, it runs to completion even
if a shorter job arrives a moment later. SRTF is the preemptive version -
if a newly arrived process has less remaining time than the one
currently running, the CPU switches to it immediately. SRTF gets an even
lower average waiting time than SJF, at the cost of more context
switches.

## Preemptive vs Non-Preemptive
Non-preemptive: once a process gets the CPU, it keeps it until it
finishes or voluntarily gives it up. Preemptive: the OS can forcibly
take the CPU away from a running process (e.g., because a
higher-priority or shorter job arrived, or a time quantum expired) and
give it to someone else.

## Why Round Robin?
It's the standard answer for time-sharing systems (like your desktop OS)
because it guarantees every process gets CPU time within a bounded
number of turns - no process waits forever, which is the "starvation"
problem non-preemptive priority scheduling can have.

## What Is Context Switching?
The act of saving a paused process's state (register values, program
counter, etc.) and loading another process's saved state, so the CPU can
switch which process it's executing. It's what makes preemption and
Round Robin possible, but it isn't free - it has real overhead, which is
part of why an extremely small time quantum is bad (too much time spent
switching, not enough running).

## How Waiting Time Is Calculated
`waitingTime = turnaroundTime - burstTime`. It's the total time a
process spent sitting in the ready queue, not running.

## How Turnaround Time Is Calculated
`turnaroundTime = completionTime - arrivalTime`. It's the total time
from when a process arrived to when it finished - waiting time plus
actual execution time.

## How the Gantt Chart Is Generated
Every scheduler records a `GanttSegment(processId, startTime, endTime)`
each time the CPU switches to a different process (or goes idle). For
preemptive algorithms, this naturally produces multiple segments for the
same process if it's paused and resumed later. Consecutive segments for
the same process with no gap are merged into one visual block for
readability.

## Time and Space Complexity

| Algorithm | Time Complexity | Notes |
|---|---|---|
| FCFS | O(n log n) | Dominated by the sort |
| SJF | O(n²) | Simple linear scan for the shortest job each iteration |
| SRTF | O(n × T) | T = total burst time; unit-by-unit simulation |
| Priority (NP) | O(n²) | Same shape as SJF |
| Priority (P) | O(n × T) | Same shape as SRTF |
| Round Robin | O(n × T / q) | q = quantum; each process gets multiple turns |

Space complexity for all algorithms is O(n) for the process list plus
O(segments) for the Gantt chart, which is at most O(n × T / q) in the
worst case for Round Robin.

## Challenges Faced
Getting Round Robin's tie-breaking right when a process arrives in the
exact same instant a time slice ends was the trickiest part - the
standard convention is to enqueue new arrivals *before* re-queuing the
process that just finished its slice, and getting that order backwards
silently changes every waiting time in the result. I also had to add
Gantt segment merging so that a process getting several consecutive
turns (because it's the only one left) renders as one clean block
instead of many tiny identical-looking ones.

## Future Improvements
Multilevel feedback queues, I/O burst simulation, exporting results, and
animated step-by-step Gantt playback (see README for the full list).
