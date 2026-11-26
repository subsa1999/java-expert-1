package org.example.fairsemaphore;

import java.util.concurrent.TimeUnit;

/**
 * USAGE
 *
 * <p>var semaphore = new FairSemaphore(2);
 * <br>
 * <p>semaphore.acquire();
 * <p>semaphore.acquire();
 * <br>
 * <p>System.out.println(semaphore.availablePermits()); // 0
 * <p>System.out.println(semaphore.tryAcquire()); // false
 * <br>
 * <p>semaphore.release();
 * <br>
 * <p>System.out.println(semaphore.availablePermits()); // 1
 * <p>System.out.println(semaphore.tryAcquire()); // true
 * <p>System.out.println(semaphore.availablePermits()); // 0
 *
 */
public interface CountingSemaphore {

  void acquire() throws InterruptedException;

  void release();

  boolean tryAcquire();

  boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

  int availablePermits();
}

