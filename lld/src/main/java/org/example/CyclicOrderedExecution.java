package org.example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicOrderedExecution {
  private int turn;
  private final int n;
  private final int k;
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition[] conditions;
  private boolean cancelled = false;

  public CyclicOrderedExecution(int n, int k) {
    if (n <= 0 || n > 26 || k <= 0) {
      throw new IllegalArgumentException("Invalid n or k");
    }

    this.n = n;
    this.k = k;
    this.conditions = new Condition[n];
    for (int i = 0; i < n; ++i) {
      conditions[i] = lock.newCondition();
    }
  }

  void print(int threadId) throws InterruptedException {
    if (threadId < 0 || threadId >= n) {
      throw new IllegalArgumentException("Invalid thread ID");
    }

    for (int i = 0; i < k; ++i){
      try {
        lock.lockInterruptibly();

        try {
          while (!cancelled && turn != threadId) {
            conditions[threadId].await();
          }

          if (cancelled) return;

          System.out.printf(
            "%d -> %c%n",
            threadId,
            (char) ('A' + threadId));

          turn = (turn + 1) % n;
          conditions[turn].signal();
        } finally {
          lock.unlock();
        }
      } catch (InterruptedException e) {
        cancel();
        throw e;
      }
    }
  }

  void cancel() throws InterruptedException {
    lock.lockInterruptibly();
    try {
      cancelled = true;

      for (Condition condition : conditions) {
        condition.signalAll();
      }
    } finally{
      lock.unlock();
    }
  }

  public static void main(String[] args){
    int n = 11;
    int k = 100;
    CyclicOrderedExecution cyclicOrderedExecution = new CyclicOrderedExecution(n, k);

    for (int i = 0; i < n; ++i) {
      final int id = i;
      Thread.ofPlatform().start(() -> {
        try {
          cyclicOrderedExecution.print(id);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }
  }
}
