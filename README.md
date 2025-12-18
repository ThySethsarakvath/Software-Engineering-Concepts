# Software Engineering Lab 07 - Threads Communication and Synchronization

## Student Information
- **Name:** THY SETHASARAKVATH
- **Student ID:** p20230035
- **Course:** Software Engineering Concepts
- **Lab:** Lab 07 - Threads Communication and Synchronization

---

## Lab Overview

This lab demonstrates practical implementation of multithreading and concurrency concepts in Java through four comprehensive tasks focusing on thread synchronization, communication, and classic concurrency problems:

1. **Thread-Safe Bank Account**
2. **Producer-Consumer with Bounded Buffer**
3. **Reader-Writer Problem**
4. **Dining Philosophers Problem**

**Estimated Time:** 5 hours

---

## Table of Contents

- [Software Engineering Lab 07 - Threads Communication and Synchronization](#software-engineering-lab-07---threads-communication-and-synchronization)
  - [Student Information](#student-information)
  - [Lab Overview](#lab-overview)
  - [Table of Contents](#table-of-contents)
  - [Task 1: Thread-Safe Bank Account](#task-1-thread-safe-bank-account)
    - [Description](#description)
    - [Files](#files)
    - [Features](#features)
    - [How to Run](#how-to-run)
    - [Output](#output)
  - [Task 2: Producer-Consumer with Bounded Buffer](#task-2-producer-consumer-with-bounded-buffer)
    - [Description](#description-1)
    - [Files](#files-1)
    - [Features](#features-1)
    - [How to Run](#how-to-run-1)
    - [Output](#output-1)
  - [Task 3: Reader-Writer Problem](#task-3-reader-writer-problem)
    - [Description](#description-2)
    - [Files](#files-2)
    - [Features](#features-2)
    - [How to Run](#how-to-run-2)
    - [Output](#output-2)
  - [Task 4: Dining Philosophers Problem](#task-4-dining-philosophers-problem)
    - [Description](#description-3)
    - [Files](#files-3)
    - [Features](#features-3)
    - [How to Run](#how-to-run-3)
    - [Output](#output-3)

---

## Task 1: Thread-Safe Bank Account

### Description
Implementation of a concurrent bank account system that handles simultaneous deposits and withdrawals without race conditions. Uses synchronization mechanisms to ensure thread safety.

### Files
- `BankAccount.java` - Thread-safe bank account with synchronization
- `Main.java` - Entry point with concurrent transaction testing

### Features
- ✅ Initial balance: $1000
- ✅ Thread-safe withdraw and deposit operations
- ✅ Uses `ReentrantLock` for mutual exclusion
- ✅ Combines `synchronized` methods with explicit locking
- ✅ Prevents race conditions during concurrent access
- ✅ Handles insufficient funds properly
- ✅ Input validation for deposit amounts

### How to Run

**Compile:**
```bash
javac bankaccount/*.java
```

**Run:**
```bash
java bankaccount.Main
```

### Output

```
Withdrawn: 600.0
Insufficient funds
Balances: 400.0
```

The output demonstrates:
- Thread 1 successfully withdraws $600 (balance: $1000 → $400)
- Thread 2 attempts to withdraw $500 but fails due to insufficient funds
- Final balance is correctly maintained at $400

---

## Task 2: Producer-Consumer with Bounded Buffer

### Description
Implementation of the classic Producer-Consumer pattern using a bounded buffer. Producers add items to the buffer while consumers remove them, with proper synchronization to handle buffer full/empty conditions.

### Files
- `ProCon.java` - Bounded buffer with synchronization logic
- `Main.java` - Entry point orchestrating producers and consumers

### Features
- ✅ Bounded buffer capacity: 5 items
- ✅ 3 producer threads generating items
- ✅ 2 consumer threads consuming items
- ✅ Each producer creates 5 items
- ✅ Proper synchronization using `synchronized`, `wait()`, and `notifyAll()`
- ✅ Producers wait when buffer is full
- ✅ Consumers wait when buffer is empty
- ✅ Real-time buffer state display
- ✅ Thread identification in output
- ✅ Graceful thread interruption and cleanup

### How to Run

**Compile:**
```bash
javac procon/*.java
```

**Run:**
```bash
java procon.Main
```

### Output

```
[Producer-1] Produced: 101 (buffer size: 1)
[Producer-2] Produced: 201 (buffer size: 2)
[Consumer-1] Consumed: 101 (buffer size: 1)
[Producer-3] Produced: 301 (buffer size: 2)
[Producer-1] Produced: 102 (buffer size: 3)
...
[Producer-1] Finished
[Producer-2] Finished
[Producer-3] Finished
[Consumer-1] Finished
[Consumer-2] Finished

Final buffer size: 0
```

The output shows:
- Producers adding items with unique IDs (format: ProducerID * 100 + item number)
- Consumers removing items in FIFO order
- Buffer size tracking after each operation
- Proper coordination between threads

---

## Task 3: Reader-Writer Problem

### Description
Implements a solution to the classic Reader-Writer problem where multiple readers can access a shared resource simultaneously, but writers require exclusive access. Uses `ReadWriteLock` for fair and efficient synchronization.

### Files
- `SharedResource.java` - Resource with read/write lock synchronization
- `Reader.java` - Reader thread implementation
- `Writer.java` - Writer thread implementation
- `Main.java` - Entry point with multiple readers and writers

### Features
- ✅ 5 reader threads (each reads 3 times)
- ✅ 3 writer threads (each writes 2 times)
- ✅ Uses `ReentrantReadWriteLock` for synchronization
- ✅ Multiple concurrent readers allowed
- ✅ Writers get exclusive access (no concurrent readers/writers)
- ✅ Tracks active reader count in real-time
- ✅ Statistics tracking (total reads, writes, counts)
- ✅ Simulated read time: 1 second
- ✅ Simulated write time: 2 seconds
- ✅ Thread-safe statistics collection
- ✅ Displays final data state

### How to Run

**Compile:**
```bash
javac readwrite/*.java
```

**Run:**
```bash
java readwrite.Main
```

### Output

```
Multiple Readers and Multiple Writers
[Reader-1] Reading data: "Initial Data" (Active readers: 2)
[Reader-2] Reading data: "Initial Data" (Active readers: 2)
[Reader-1] Finished reading
[Reader-2] Finished reading
[Writer-1] Writing new data: "Data from Writer-1 (write #1)" (Write #1)
[Writer-1] Finished writing
[Reader-3] Reading data: "Data from Writer-1 (write #1)" (Active readers: 1)
...
[Reader-5] Completed all reads
[Writer-3] Completed all writes

Test completed - Proper read/write synchronization
Total reads: 15
Total writes: 6
Final data: "Data from Writer-3 (write #2)"
```

The output demonstrates:
- Multiple readers accessing resource concurrently
- Writers waiting for exclusive access
- Active reader count tracking
- Proper synchronization preventing conflicts
- Statistics summary with final state

---

## Task 4: Dining Philosophers Problem

### Description
Implementation of the classic Dining Philosophers problem with 5 philosophers sharing 5 forks. Uses timeout-based deadlock prevention to ensure each philosopher can eat without system deadlock.

### Files
- `Philosopher.java` - Philosopher thread with state machine and fork acquisition logic
- `PhilosopherStatistics.java` - Thread-safe statistics tracking
- `DiningTable.java` - Table setup and simulation orchestration
- `Main.java` - Entry point with 2-minute simulation

### Features
- ✅ 5 philosophers sitting around a circular table
- ✅ 5 forks (ReentrantLock) shared between philosophers
- ✅ Three states: THINKING, HUNGRY, EATING
- ✅ Each philosopher alternates between thinking and eating
- ✅ Requires both left and right forks to eat
- ✅ Timeout mechanism (100ms) prevents deadlock
- ✅ Each philosopher eats at least 3 times
- ✅ Simulation runs for 2 minutes (120 seconds)
- ✅ State display every 15 seconds
- ✅ Comprehensive statistics tracking:
  - Think/Hungry/Eat counts
  - Average times for each state
  - Timeout events
  - Total meals served
- ✅ Requirements validation at end

### How to Run

**Compile:**
```bash
javac dining_problem/*.java
```

**Run:**
```bash
java dining_problem.Main
```

### Output

```
=== Dining Philosophers Problem ===
Starting simulation with 5 philosophers
Each philosopher needs both left and right forks to eat
Using timeout mechanism to prevent deadlock

[Philosopher 0] Joined the table
[Philosopher 1] Joined the table
[Philosopher 2] Joined the table
[Philosopher 3] Joined the table
[Philosopher 4] Joined the table
[Philosopher 0] Thinking...
[Philosopher 1] Thinking...
[Philosopher 2] Thinking...
[Philosopher 3] Thinking...
[Philosopher 4] Thinking...
[Philosopher 0] Hungry, trying to pick up forks...
[Philosopher 0] Picked up left fork
[Philosopher 0] Picked up right fork
[Philosopher 0] EATING (meal #1)
...

Time elapsed: 15 seconds
======================================================================
Current State:
  Philosopher 0: THINKING | Stats[Ate: 3, Thought: 4, Timeouts: 2, Avg Wait: 125ms]
  Philosopher 1: EATING | Stats[Ate: 2, Thought: 3, Timeouts: 1, Avg Wait: 98ms]
  Philosopher 2: HUNGRY | Stats[Ate: 3, Thought: 4, Timeouts: 3, Avg Wait: 156ms]
  Philosopher 3: THINKING | Stats[Ate: 2, Thought: 3, Timeouts: 0, Avg Wait: 87ms]
  Philosopher 4: EATING | Stats[Ate: 3, Thought: 4, Timeouts: 2, Avg Wait: 112ms]
======================================================================

...

=== Stopping simulation ===
[Philosopher 0] Left the table - Stats[Ate: 15, Thought: 16, Timeouts: 8, Avg Wait: 145ms]
[Philosopher 1] Left the table - Stats[Ate: 14, Thought: 15, Timeouts: 6, Avg Wait: 132ms]
[Philosopher 2] Left the table - Stats[Ate: 13, Thought: 14, Timeouts: 9, Avg Wait: 167ms]
[Philosopher 3] Left the table - Stats[Ate: 15, Thought: 16, Timeouts: 5, Avg Wait: 121ms]
[Philosopher 4] Left the table - Stats[Ate: 14, Thought: 15, Timeouts: 7, Avg Wait: 138ms]

======================================================================
FINAL STATISTICS
======================================================================

Philosopher 0:
  Think: 16 times (avg 987ms), Hungry: 15 times (avg wait 145ms), Ate: 15 times (avg 1456ms), Timeouts: 8

Philosopher 1:
  Think: 15 times (avg 1023ms), Hungry: 14 times (avg wait 132ms), Ate: 14 times (avg 1398ms), Timeouts: 6

Philosopher 2:
  Think: 14 times (avg 956ms), Hungry: 13 times (avg wait 167ms), Ate: 13 times (avg 1512ms), Timeouts: 9

Philosopher 3:
  Think: 16 times (avg 1045ms), Hungry: 15 times (avg wait 121ms), Ate: 15 times (avg 1423ms), Timeouts: 5

Philosopher 4:
  Think: 15 times (avg 989ms), Hungry: 14 times (avg wait 138ms), Ate: 14 times (avg 1467ms), Timeouts: 7

----------------------------------------------------------------------
SUMMARY:
  Total meals served: 71
  Average meals per philosopher: 14
  Min meals: 13
  Max meals: 15
  Total timeouts (deadlock prevention): 35
  Average wait time: 140ms

----------------------------------------------------------------------
REQUIREMENTS CHECK:
- Each philosopher ate at least 3 times: PASSED
- No deadlock occurred: PASSED (timeout mechanism worked)
- No starvation: PASSED
======================================================================
```

The output demonstrates:
- Real-time philosopher state changes
- Periodic state snapshots every 15 seconds
- Fork acquisition with timeout handling
- Deadlock prevention through timeouts
- Comprehensive final statistics
- Validation that all requirements are met

---

**End of README**