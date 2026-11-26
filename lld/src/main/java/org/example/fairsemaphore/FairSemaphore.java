package org.example.fairsemaphore;

import java.util.ArrayDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FairSemaphore implements CountingSemaphore {
  private final int maxPermits;
  private int permits;
  private final ReentrantLock lock;
  private final Condition notAvailable;
  private final ArrayDeque<Thread> waitingThreads;

  public FairSemaphore(int maxPermits) {
    if (maxPermits <= 0) {
      throw new IllegalStateException("MAX permits can not be zero or negative");
    }

    this.maxPermits = maxPermits;
    this.lock = new ReentrantLock();
    this.waitingThreads = new ArrayDeque<>();
    this.notAvailable = lock.newCondition();
  }

  @Override
  public void acquire() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      Thread currentThread = Thread.currentThread();
      try {
        waitingThreads.addLast(currentThread);

        while (permits == 0 || waitingThreads.peekFirst() != currentThread) {
          notAvailable.await();
        }

        // NOTE: You can come to this when the above
        // while two conditions both are false at the same time.
        // Then current thread gets wake up.
        waitingThreads.pollFirst();
        permits -= 1;

        if (permits > 0) {
          notAvailable.signalAll();
        }

        // If whiling waiting in the queue,
        // lets think a situation where the curent thread got interrupted.
        // then we shall remove this thread from waiting queue, otherwise
        // all newer threads, indefinitely will be waiting.
      } catch (InterruptedException e) {
        waitingThreads.remove(currentThread);

        // We have removed a thread from the queue,
        // now if permit has become non-zero then we shall invoke signal so that
        // another thread can get a chance.
        // the permit != 0 is not tied to removal of the current thread
        // this is just an optimization
        if (permits > 0) notAvailable.signalAll();
        throw e;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void release() {
    // Why not lock-interruptibly?
    // Because 'release' is more of a clean-up operation.
    // So if the current thread (who is calling release()) is interrupted
    // still we need to release and return;
    // Otherwise if we are not releasing permit for an already interrupted thread
    // it will cause resource leakage.
    lock.lock();

    try {
      if (permits == maxPermits) {
        throw new IllegalStateException("Permits are already at MAX");
      }

      permits += 1;
      notAvailable.signalAll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean tryAcquire() {
    if (!lock.tryLock()) return false;

    try {
      if (permits > 0 && waitingThreads.isEmpty()) {
        permits -= 1;
        return true;
      }

      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
    long remaining = unit.toNanos(timeout);
    long startTime = System.nanoTime();

    if (!lock.tryLock(remaining, TimeUnit.NANOSECONDS)) {
      return false;
    }

    Thread currentThread = Thread.currentThread();

    try {
      remaining -= System.nanoTime() - startTime;

      waitingThreads.addLast(currentThread);

      while (permits == 0 || waitingThreads.peekFirst() != currentThread) {
        if (remaining <= 0) return false;
        remaining = notAvailable.awaitNanos(remaining);
      }

      permits -= 1;
      return true;
    } finally {
      waitingThreads.remove(currentThread);

      if (permits > 0) {
        notAvailable.signalAll();
      }
      lock.unlock();
    }
  }

  @Override
  public int availablePermits() {
    lock.lock();
    try {
      return permits;
    } finally{
      lock.unlock();
    }
  }
}
