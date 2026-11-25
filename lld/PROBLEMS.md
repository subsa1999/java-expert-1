# **3-Day Java Concurrency & LLD Challenge**

**45 implementation problems targeting SDE-III / Senior Software Engineer interviews.**

These are not conventional LeetCode problems. They combine data structures, low-level design, Java concurrency, synchronization, resource management and correctness under multithreaded execution.

Given your experience with distributed systems and backend engineering, the emphasis is on implementing infrastructure components from scratch rather than conventional application LLDs like Parking Lot or Splitwise.

| Day   | Focus                                    | Problems |
| ----- | ---------------------------------------- | -------- |
| Day 1 | Synchronization & concurrency primitives | 15       |
| Day 2 | Concurrent data structures & algorithms  | 15       |
| Day 3 | Concurrent backend infrastructure & LLD  | 15       |

Use **Java 21**. Avoid built-in implementations of the component you are being asked to design.

Difficulty notation: **H** = Hard, **VH** = Very Hard.

# **Day 1 — Concurrency primitives & synchronization**

## **1. Cyclic ordered execution with N threads — H**

Given N threads, print `ABC...NABC...N` for K cycles.

**Implementation steps:**

- Maintain a shared state representing the current thread's turn.
- Create N separate `Condition` objects using `ReentrantLock`.
- Wake only the next eligible thread.
- Implement graceful termination and interruption handling.

**Challenge:** Support dynamically registering and removing participants without breaking execution order.

## **2. Bounded blocking queue — H**

```
interface BlockingQueue<T> {
    void put(T item) throws InterruptedException;
    T take() throws InterruptedException;
    boolean offer(T item, long timeout, TimeUnit unit)
        throws InterruptedException;
    int size();
}
```

**Hints & steps:**

- Implement a circular array.
- Maintain head, tail and count.
- Use `notEmpty` and `notFull` conditions.
- Handle spurious wakeups and timeout accounting.

**Challenge:** Multiple producers and consumers with graceful shutdown.

## **3. Fair counting semaphore — H**

Implement your own semaphore without using Java's `Semaphore`.

**Hints & steps:**

- Maintain available permits.
- Maintain a FIFO queue of waiting threads.
- Implement `acquire()`, `release()` and `tryAcquire()`.
- Remove interrupted waiters safely.

**Challenge:** Support acquiring multiple permits atomically without starving large requests.

## **4. Reentrant read-write lock — VH**

Implement a lock that allows concurrent readers but exclusive writers.

**Hints & steps:**

- Track active readers, waiting writers and the current writer.
- Maintain per-thread read acquisition counts.
- Support write-lock reentrancy.
- Implement writer-to-reader lock downgrading.

**Challenge:** Prevent writer starvation while retaining good read throughput.

## **5. Cyclic barrier — H**

N threads must finish their current phase before any thread can proceed to the next phase.

**Hints & steps:**

- Track participant count and generation number.
- Block threads until all participants arrive.
- Reset the count for the next generation.
- Execute an optional barrier action.

**Challenge:** Handle a broken barrier when one participant times out or gets interrupted.

## **6. Countdown latch — H**

Implement `CountDownLatch` from scratch.

```
void await();
boolean await(long timeout, TimeUnit unit);
void countDown();
long getCount();
```

**Hints & steps:**

- Protect the counter using a lock or CAS.
- Block waiting threads while count is positive.
- Wake all waiters when the count reaches zero.
- Ensure the count never becomes negative.

**Challenge:** Implement it again using `AbstractQueuedSynchronizer`.

## **7. Thread pool executor — VH**

Implement a fixed-size thread pool without using `ExecutorService`.

**Hints & steps:**

- Build a bounded task queue.
- Create reusable worker threads.
- Implement task submission and rejection policies.
- Introduce `RUNNING`, `SHUTDOWN`, `STOP` and `TERMINATED` states.

**Challenge:** Dynamic worker scaling, idle worker expiry, graceful and immediate shutdown.

## **8. Future and Promise — VH**

Implement your own asynchronous result abstraction.

```
interface MyFuture<T> {
    T get() throws Exception;
    boolean cancel(boolean interrupt);
    boolean isDone();
}
```

**Hints & steps:**

- Model pending, running, successful, failed and cancelled states.
- Store results or exceptions.
- Block callers of `get()` until a terminal state.
- Resolve completion-versus-cancellation races.

**Challenge:** Implement `thenApply()`, `thenCompose()` and `allOf()`.

## **9. Scheduled task executor — VH**

Implement delayed, periodic and one-time task execution.

**Hints & steps:**

- Use a min-heap ordered by execution deadline.
- Maintain a scheduler thread.
- Wake the scheduler when an earlier task arrives.
- Delegate execution to a worker pool.

**Challenge:** Implement both fixed-rate and fixed-delay scheduling, including cancellation.

## **10. Dining philosophers — H**

Five philosophers share five forks. A philosopher needs both adjacent forks to eat.

**Hints & steps:**

- Model each fork as a lock.
- Establish a global lock acquisition order.
- Alternatively, limit concurrent contenders using a semaphore.
- Verify deadlock freedom.

**Challenge:** Guarantee starvation freedom.

## **11. Concurrent H2O formation — H**

Hydrogen and oxygen threads arrive randomly. Release them in groups containing exactly two hydrogen and one oxygen.

**Hints & steps:**

- Maintain separate hydrogen and oxygen permits.
- Use a barrier to synchronize the selected participants.
- Release permits for the next molecule only after completion.

**Challenge:** Generalize it to arbitrary molecular compositions.

## **12. Multithreaded merge sort — H**

Implement parallel merge sort for a large array.

**Hints & steps:**

- Recursively partition the input.
- Execute independent partitions concurrently.
- Merge only after both child tasks finish.
- Introduce a sequential execution threshold.

**Challenge:** Implement using `ForkJoinPool`, then using your own thread pool without thread-starvation deadlock.

## **13. Deadlock detecting lock manager — VH**

Build a lock manager that detects deadlocks before granting incompatible lock requests.

**Hints & steps:**

- Maintain resource ownership.
- Build a wait-for graph.
- Detect cycles using DFS.
- Reject or abort a selected participant.

**Challenge:** Support shared and exclusive locks, timeout and transaction rollback integration.

## **14. Striped lock manager — H**

Implement a mechanism for synchronizing operations on millions of different keys without creating millions of locks.

**Hints & steps:**

- Preallocate a fixed number of locks.
- Map keys to lock stripes using hashing.
- Implement single-key and multi-key locking.
- Acquire distinct stripes in deterministic order.

**Challenge:** Design safe dynamic stripe resizing.

## **15. Lock-free stack — VH**

Implement a concurrent stack without `synchronized` or `ReentrantLock`.

**Hints & steps:**

- Represent the head using `AtomicReference`.
- Implement push and pop using CAS retry loops.
- Identify linearization points.
- Understand when the ABA problem can occur.

**Challenge:** Demonstrate an ABA scenario with reused nodes and address it using `AtomicStampedReference`.

# **Day 2 — Concurrent data structures & algorithms**

## **16. Thread-safe LRU cache — H**

Implement a cache with O(1) `get()` and `put()`.

**Hints & steps:**

- Combine HashMap and doubly linked list.
- Protect map and list mutations within the same critical section.
- Evict the least recently used entry.
- Remember that `get()` also modifies the LRU ordering.

**Challenge:** Implement a segmented LRU cache to improve concurrent throughput, accepting approximate global LRU ordering.

## **17. Thread-safe LFU cache — VH**

Implement O(1) LFU eviction with LRU as the tie-breaker.

**Hints & steps:**

- Maintain a key-to-node map.
- Maintain frequency-to-doubly-linked-list mappings.
- Track minimum frequency.
- Synchronize frequency promotion and eviction.

**Challenge:** Implement frequency ageing to prevent historically popular keys from remaining forever.

## **18. Concurrent HashMap from scratch — VH**

```
interface ConcurrentMap<K, V> {
    V get(K key);
    void put(K key, V value);
    V remove(K key);
    V putIfAbsent(K key, V value);
}
```

**Hints & steps:**

- Implement hash buckets and collision handling.
- Introduce bucket-level or segment-level locking.
- Ensure safe publication and visibility.
- Implement thread-safe resizing.

**Challenge:** Make reads lock-free and support concurrent resizing.

## **19. Lock-free concurrent queue — VH**

Implement an unbounded FIFO queue using atomic operations.

**Hints & steps:**

- Study the Michael–Scott queue algorithm.
- Introduce atomic head and tail references.
- Initialize using a dummy node.
- Use CAS to append and remove nodes.
- Allow threads to help advance a lagging tail.

**Challenge:** Prove linearizability for concurrent producers and consumers.

## **20. Concurrent priority blocking queue — H**

Implement a blocking priority queue backed by a binary heap.

**Hints & steps:**

- Implement heap insertion and removal.
- Protect heap modifications with a lock.
- Use conditions to block consumers.
- Introduce sequence numbers for equal-priority elements.

**Challenge:** Support priority updates and arbitrary task cancellation in O(log N).

## **21. Concurrent snapshot key-value store — VH**

```
void put(String key, String value);
long snapshot();
String get(String key, long snapshotId);
void delete(String key);
```

**Hints & steps:**

- Assign monotonically increasing versions.
- Maintain ordered version history per key.
- Use binary search or `TreeMap.floorEntry()`.
- Ensure snapshots capture a consistent publication boundary.

**Challenge:** Support atomic multi-key writes and garbage collection of expired snapshots.

## **22. Concurrent TTL cache — H**

Build a key-value store where every entry can have an independent expiration time.

**Hints & steps:**

- Store values alongside expiration timestamps.
- Implement lazy expiration during reads.
- Maintain a min-heap or delay queue for proactive cleanup.
- Associate each expiration event with an entry version.

**Challenge:** Ensure an old expiration event cannot accidentally delete a newly updated value.

## **23. Concurrent Trie autocomplete — H**

Implement a Trie supporting concurrent insertion, deletion and prefix searches.

**Hints & steps:**

- Design Trie nodes with child mappings.
- Introduce read-write locks or immutable nodes.
- Maintain word frequency for ranking.
- Use a bounded heap for top-K suggestions.

**Challenge:** Provide lock-free searches using immutable Trie nodes and atomic root replacement.

## **24. Concurrent streaming median — H**

Multiple threads insert integers continuously while other threads query the median.

**Hints & steps:**

- Maintain a max-heap and min-heap.
- Preserve size and ordering invariants.
- Synchronize insertion and heap rebalancing.
- Ensure a median query sees a consistent state of both heaps.

**Challenge:** Partition ingestion across workers and investigate the trade-offs of exact versus approximate median.

## **25. Concurrent sliding-window Top-K — VH**

Return the top K frequently occurring items during the last five minutes.

```
void record(String item, long timestamp);
List<String> topK(int k);
```

**Hints & steps:**

- Partition events into time buckets.
- Maintain frequency counters.
- Expire old buckets and decrement aggregate frequencies.
- Maintain a heap or ordered frequency index.

**Challenge:** Handle late-arriving events and simultaneous ingestion, expiration and queries.

## **26. Concurrent interval reservation system — VH**

Multiple threads attempt to reserve overlapping time intervals for the same resource.

```
boolean reserve(String resource, long start, long end);
boolean cancel(String reservationId);
```

**Hints & steps:**

- Maintain a `TreeMap` of intervals per resource.
- Detect overlaps using predecessor and successor intervals.
- Make validation and insertion atomic.
- Introduce per-resource synchronization.

**Challenge:** Reserve multiple resources atomically without deadlock.

## **27. Concurrent Union-Find — VH**

Implement a thread-safe disjoint-set data structure.

**Hints & steps:**

- Implement path compression and union by size.
- Start with a global-lock implementation.
- Move to root-level synchronization.
- Revalidate roots after acquiring locks.
- Use deterministic lock ordering.

**Challenge:** Explore a CAS-based version that cannot create parent cycles.

## **28. Concurrent web crawler — H**

Given a starting URL and a mock HTML parser, crawl all reachable pages within the same domain.

**Hints & steps:**

- Use a concurrent visited set.
- Atomically register newly discovered URLs.
- Distribute work across a thread pool.
- Track outstanding tasks to determine completion.

**Challenge:** Add per-domain rate limiting, bounded memory, retries and cancellation.

## **29. Parallel dependency graph executor — VH**

Execute a DAG of tasks while maximizing concurrency.

**Hints & steps:**

- Build an adjacency list and in-degree counters.
- Detect cycles before execution.
- Submit all zero-in-degree tasks.
- Atomically decrement dependent task counters after completion.
- Submit newly eligible tasks exactly once.

**Challenge:** Support retries, failure propagation, task priorities and independent execution branches.

## **30. Concurrent order matching engine — VH**

Implement a simplified stock exchange order book.

**Hints & steps:**

- Maintain buy and sell price levels using ordered maps.
- Maintain FIFO ordering within each price level.
- Implement price-time priority.
- Support order cancellation and partial fills.
- Serialize matching operations per instrument.

**Challenge:** Support concurrent instruments, atomic order modification and deterministic trade generation.

# **Day 3 — Concurrent backend infrastructure & LLD**

## **31. Multistrategy rate limiter — H**

Implement the following rate-limiting algorithms:

- Fixed window
- Sliding window log
- Sliding window counter
- Token bucket
- Leaky bucket

**Hints & steps:** Define a common `RateLimiter` interface. Maintain independent state per client. Make refill, validation and token consumption atomic. Use a monotonic clock for elapsed time.

**Challenge:** Implement hierarchical limits at user, API and organization levels.

## **32. Database connection pool — VH**

```
Connection acquire(Duration timeout);
void release(Connection connection);
void shutdown();
```

**Hints & steps:**

- Maintain available and leased connections.
- Enforce maximum pool capacity.
- Block callers when the pool is exhausted.
- Validate unhealthy connections.
- Prevent double releases.

**Challenge:** Dynamic pool resizing, connection leak detection and graceful shutdown.

## **33. Circuit breaker — H**

**Hints & steps:**

- Implement the three states.
- Maintain a rolling failure counter.
- Atomically transition between states.
- Restrict concurrent probe requests in half-open state.

**Challenge:** Prevent stale responses from a previous state from corrupting the current state.

## **34. Concurrent in-memory message broker — VH**

Implement topics, partitions, producers and consumer groups.

**Hints & steps:**

- Model each partition as an append-only log.
- Assign monotonically increasing offsets.
- Maintain consumer-group offsets.
- Implement blocking consumption.
- Ensure one active partition owner per consumer group.

**Challenge:** Consumer rebalancing, acknowledgement, redelivery and stale-consumer fencing.

## **35. In-memory pub-sub event bus — H**

```
void subscribe(String topic, Subscriber subscriber);
void unsubscribe(String topic, Subscriber subscriber);
void publish(String topic, Event event);
```

**Hints & steps:**

- Maintain topic-to-subscriber mappings.
- Dispatch callbacks asynchronously.
- Never execute subscriber callbacks while holding a registry lock.
- Isolate subscriber failures.

**Challenge:** Per-subscriber ordering, bounded queues and configurable backpressure policies.

## **36. Concurrent inventory reservation engine — VH**

Given limited inventory, support thousands of simultaneous purchase requests without overselling.

**Hints & steps:**

- Maintain available and reserved quantities.
- Atomically validate and reserve stock.
- Assign reservation IDs and expiration times.
- Implement confirm, cancel and expire state transitions.

**Challenge:** Resolve payment confirmation versus reservation expiry races. Support atomic multi-product reservations.

## **37. Idempotency execution engine — VH**

```
<T> T execute(String idempotencyKey, Callable<T> action);
```

Multiple threads may submit the same operation simultaneously. Only one should execute it within the process.

**Hints & steps:**

- Map idempotency keys to futures or execution states.
- Atomically register the first caller.
- Make duplicate callers wait for the same result.
- Validate request fingerprints.

**Challenge:** Handle failed execution, retries, TTL eviction and process crashes. Discuss why in-memory synchronization alone cannot guarantee exactly-once external side effects.

## **38. Concurrent task job scheduler — VH**

Implement a job scheduler supporting delayed tasks, priorities, retries and dependencies.

**Hints & steps:**

- Model job lifecycle using a state machine.
- Maintain ready and delayed task queues.
- Implement worker dispatch.
- Track retry attempts and exponential backoff.
- Make job claiming atomic.

**Challenge:** Implement cancellation of running jobs, worker leases and dead-letter queues.

## **39. Asynchronous logging framework — H**

Implement a high-throughput logging framework.

**Hints & steps:**

- Define logger, formatter, appender and filter interfaces.
- Use a bounded concurrent buffer.
- Batch log writes through a background thread.
- Implement configurable log levels.

**Challenge:** Handle queue overflow, file rotation, flushing, slow disk and graceful termination.

## **40. Concurrent banking transaction engine — VH**

```
void deposit(String account, long amount);
void withdraw(String account, long amount);
void transfer(String from, String to, long amount);
```

**Hints & steps:**

- Introduce account-level locking.
- Enforce non-negative balances.
- Acquire multiple account locks in deterministic order.
- Record transaction history.
- Make transfers atomic.

**Challenge:** Implement multi-account transactions, idempotency and consistent balance snapshots.

## **41. Multithreaded WAL-based key-value database — VH**

Build a persistent key-value store that survives process restarts.

Concurrent writers

Write coordinator

Append WAL + fsync

Update in-memory index

Acknowledge write

**Hints & steps:**

- Assign ordered log sequence numbers.
- Serialize WAL records and append them to disk.
- Establish durable write and visibility boundaries.
- Replay the WAL during startup.
- Handle incomplete records using length and checksum validation.

**Challenge:** Group commits, concurrent reads, checkpoints and safe WAL truncation.

## **42. MVCC transaction engine — VH**

Implement snapshot-isolated transactions.

```
Transaction begin();
void put(Transaction tx, String key, String value);
String get(Transaction tx, String key);
boolean commit(Transaction tx);
void rollback(Transaction tx);
```

**Hints & steps:**

- Assign transaction start timestamps.
- Maintain committed versions per key.
- Buffer uncommitted writes.
- Read the latest visible version for the transaction.
- Detect write-write conflicts during atomic commit.

**Challenge:** Implement version garbage collection and demonstrate write skew under snapshot isolation.

## **43. Concurrent data migration pipeline — VH**

Implement a migration engine that copies millions of records while writes continue on the source.

**Hints & steps:**

- Partition source records into independent ranges.
- Use a producer-consumer pipeline.
- Maintain durable per-partition checkpoints.
- Capture changes through a simulated CDC stream.
- Use version-aware, idempotent destination upserts.

**Challenge:** Handle out-of-order events, deletes, worker crashes and backfill-versus-CDC conflicts.

## **44. Concurrent metrics aggregation engine — VH**

Implement a service ingesting metrics from multiple producers.

```
void record(String metric, double value, long timestamp);
MetricResult query(String metric, long start, long end);
```

**Hints & steps:**

- Partition metrics using consistent key hashing or fixed shards.
- Aggregate into time buckets.
- Maintain count, sum, minimum and maximum.
- Support concurrent queries and ingestion.
- Implement retention-based eviction.

**Challenge:** Support late events, concurrent bucket rotation, percentile approximation and hot-key mitigation.

## **45. Concurrent DAG-based AI agent execution engine — VH**

Build an execution framework for independent and dependent tool calls.

**Hints & steps:**

- Define `Agent`, `Tool`, `Task`, `ExecutionContext` and `ExecutionEngine` abstractions.
- Model dependencies using a DAG.
- Execute independent tool calls concurrently.
- Support per-tool concurrency limits.
- Implement timeout, cancellation and retry policies.
- Merge results into a consistent execution context.

**Challenge:** Dynamic task generation, shared-state conflicts, backpressure, execution budgets and failure propagation.

# **Execution strategy for the next 3 days**

Completing all 45 production-quality implementations in three days is unrealistic. Aim for **15 complete implementations and 30 time-boxed design or implementation exercises.**

## **Top 15 — Non-negotiable problems**

# **0/15**

0% completed

**Day 1 — Core concurrency**

2. Bounded blocking queue

4. Reentrant read-write lock

7. Thread pool executor

8. Future and Promise

9. Scheduled task executor

**Day 2 — Data structures**

16. Concurrent LRU cache

18. Concurrent HashMap

19. Lock-free queue

21. Snapshot key-value store

29. Parallel DAG executor

**Day 3 — Backend infrastructure**

32. Connection pool

34. In-memory message broker

37. Idempotency engine

41. WAL-based database

42. MVCC transaction engine

## **Java concepts you must cover**

| Concept                              | Target problems |
| ------------------------------------ | --------------- |
| `synchronized`, `wait`, `notifyAll`  | 2, 5, 6         |
| `ReentrantLock`, `Condition`         | 1, 2, 3, 4      |
| Atomic variables and CAS             | 15, 19, 27      |
| Java Memory Model and happens-before | 6, 8, 18        |
| `AbstractQueuedSynchronizer`         | 3, 4, 6         |
| `ExecutorService`, `Future`          | 7, 8, 9         |
| `CompletableFuture`                  | 8, 29, 45       |
| `ForkJoinPool`                       | 12              |
| Virtual threads                      | 28, 32, 45      |
| Deadlock and starvation              | 4, 10, 13, 40   |
| Lock striping                        | 14, 16, 18      |
| Linearizability                      | 15, 19, 21      |
| Backpressure                         | 7, 34, 35, 39   |
| State machine                        | 8, 33, 36, 38   |
| Durability and crash recovery        | 41, 42, 43      |

## **Daily time allocation**

5 priority implementations

**5 hours**

10 time-boxed exercises

**3 hours 20 min**

Concurrency testing

**1 hour**

Theory and revision

**40 min**

Total: 10 focused hours per day, excluding breaks.

## **Mandatory correctness checklist**

For every priority implementation, answer these questions before considering it complete.

**Implementation review**

0/12Reset

What are the shared mutable states?

What invariants must always hold?

What is the linearization point of each operation?

What happens-before relationships guarantee visibility?

Can two threads cause a race condition?

Can deadlock, livelock or starvation occur?

What happens when a thread gets interrupted?

How are timeout and cancellation handled?

What happens under 1,000 concurrent requests?

Where is the performance bottleneck?

Can coarse-grained locking be replaced with finer-grained locking?

How would I test correctness deterministically?

## **Standard concurrency test harness**

Use this as a starting point for load-testing your implementations.

```
int threads = 1000;

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

    var start = new CountDownLatch(1);
    var futures = new ArrayList<Future<?>>();

    for (int i = 0; i < threads; i++) {
        futures.add(executor.submit(() -> {
            start.await();

            // Invoke the operation under test.

            return null;
        }));
    }

    start.countDown();

    for (var future : futures) {
        future.get(10, TimeUnit.SECONDS);
    }
}

// Assert final state and invariants.
```

This harness is useful for stress testing but does not prove thread safety. Also write deterministic interleaving tests using latches and barriers, and use JCStress for Java Memory Model-sensitive implementations.
