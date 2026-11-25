package org.example;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueTest {
  public interface BlockingQueue<T> {
    void put(T item) throws InterruptedException;

    T take() throws InterruptedException;

    boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException;

    int size();
  }

  public interface SafeShutdownEligible {
    void shutdown();

    boolean isShutdown();
  }

  public static class CircularArray<T> {
    private final T[] buffer;
    private int head;
    private int tail;
    private int count = 0;

    @SuppressWarnings("unchecked")
    public CircularArray(int n) {
      buffer = (T[]) new Object[n];
    }

    public boolean add(T item) {
      if (isFull()) {
        return false;
      }

      buffer[tail] = item;
      tail = (tail + 1) % buffer.length;
      count++;
      return true;
    }

    public T remove() {
      if (isEmpty()) {
        return null;
      }

      T headVal = buffer[head];
      buffer[head] = null; // Garbage Collection
      head = (head + 1) % buffer.length;
      count--;
      return headVal;
    }

    public boolean isFull() {
      return count == buffer.length;
    }

    public boolean isEmpty() {
      return count == 0;
    }

    public int size() {
      return count;
    }
  }

  public static class BlockingQueueImpl<T> implements BlockingQueue<T>, SafeShutdownEligible {
    private final CircularArray<T> array;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private boolean shutdown;

    public BlockingQueueImpl(int cap) {
      if (cap <= 0) {
        throw new IllegalArgumentException("Capacity of a blocking queue can not be null");
      }

      this.array = new CircularArray<>(cap);
    }

    @Override
    public void put(T item) throws InterruptedException {
      Objects.requireNonNull(item);

      lock.lockInterruptibly();

      try {
        while (array.isFull() && !shutdown) {
          notFull.await();
        }

        if (shutdown) {
          throw new IllegalStateException(
              "Queue is shutdown and no longer accepting new item");
        }

        array.add(item);
        notEmpty.signal();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public T take() throws InterruptedException {
      lock.lockInterruptibly();

      try {
        while (!shutdown && array.isEmpty()) {
          notEmpty.await();
        }

        if (shutdown && array.isEmpty()) {
          throw new IllegalStateException("Queue is shut down and empty");
        }

        T removed = array.remove();
        notFull.signal();
        return removed;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public boolean offer(T item, long timeout, TimeUnit unit) throws InterruptedException {
      Objects.requireNonNull(item);

      long remaining = unit.toNanos(timeout);
      long start = System.nanoTime();

      if (!lock.tryLock(timeout, unit)) {
        return false;
      }

      try {
        remaining -= System.nanoTime() - start; // reduce time wasted due to acquiring lock.

        while (array.isFull() && !shutdown) {
          if (remaining <= 0) return false;

          // This line ensures that it waits if awaken then it returns remaining
          remaining = notFull.awaitNanos(remaining);
        }

        if (shutdown) return false;

        array.add(item);
        notEmpty.signal();
        return true;
      } finally {
        lock.unlock();
      }
    }

    @Override
    public int size() {
      lock.lock();
      try {
        return array.size();
      } finally {
        lock.unlock();
        ;
      }
    }

    @Override
    public void shutdown() {
      lock.lock();
      try {
        shutdown = true;

        notFull.signalAll();
        notEmpty.signalAll();
      } finally {
        lock.unlock();
        ;
      }
    }

    @Override
    public boolean isShutdown() {
      lock.lock();
      try {
        return shutdown;
      } finally {
        lock.unlock();
      }
    }
  }
}
