package me.broot.benchmark.fibgen;

import me.broot.benchmark.fibgen.util.ThreadType;
import me.broot.benchmark.fibgen.util.UtilsKt;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Benchmark using the `ReentrantLock` and `Condition`.
 * <p>
 * We start two threads, enter the critical section by both of them and use <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/Condition.html">Condition</a>
 * to alternate execution between them. Threads never run in parallel, the execution is fully sequential.
 * <p>
 * This is one of very few (the only one?) places in the Java Thread API that explicitly mentions atomically suspending
 * the current thread and releasing the lock (so resuming another thread).
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(LockConditionBench.ITERATIONS)
@SuppressWarnings({"unused", "CatchMayIgnoreException"})
public class LockConditionBench {

    public static final int ITERATIONS = 100000;

    @Param({"PLATFORM", "VIRTUAL"})
    private ThreadType threadType;
    @Param({"1"})
    private float multiplier;

    private Lock lock;
    private Condition cond;
    private Long item;

    private Thread producerThread;

    @Setup(Level.Invocation)
    public void beforeInvocation() {
        lock = new ReentrantLock();
        cond = lock.newCondition();
        item = null;

        producerThread = threadType.getThreadBuilder().start(() -> {
            lock.lock();
            try {
                cond.signal();
                cond.await();
                cond.signal();

                var x = 0L;
                var y = 1L;
                //noinspection InfiniteLoopStatement
                while (true) {
                    while (item != null) cond.await();
                    item = x;
                    cond.signal();
                    var z = x + y;
                    //noinspection SuspiciousNameCombination
                    x = y;
                    y = z;
                }
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        });
    }

    @Benchmark
    public void each(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            lock.lock();
            try {
                cond.signal();
                cond.await();
                cond.signal();

                for (var i = getIterations(); i > 0; i--) {
                    while (item == null) cond.await();
                    bh.consume(item.longValue());
                    item = null;
                    cond.signal();
                }
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        }).join();
    }

    @Benchmark
    public void sum(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            lock.lock();
            try {
                cond.signal();
                cond.await();
                cond.signal();

                var sum = 0L;
                for (var i = getIterations(); i > 0; i--) {
                    while (item == null) cond.await();
                    sum += item;
                    item = null;
                    cond.signal();
                }

                UtilsKt.checkFibSum(getIterations(), sum);
                bh.consume(sum);
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        }).join();
    }

    @TearDown(Level.Invocation)
    public void afterInvocation() throws InterruptedException {
        producerThread.interrupt();
        producerThread.join();
    }

    private int getIterations() {
        return (int) (ITERATIONS * multiplier);
    }
}
