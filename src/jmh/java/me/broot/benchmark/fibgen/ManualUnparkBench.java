package me.broot.benchmark.fibgen;

import me.broot.benchmark.fibgen.util.ThreadType;
import me.broot.benchmark.fibgen.util.UtilsKt;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.locks.LockSupport;

/**
 * Benchmark that manually parks and unparks threads.
 * <p>
 * We start two threads, then use <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/LockSupport.html#park()">LockSupport.park</a> and <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/LockSupport.html#unpark(java.lang.Thread)">LockSupport.unpark</a>
 * to alternate the execution between them.
 * <p>
 * Both threads run closely to sequential, but it is not possible to atomically park the current thread and unpark
 * another one, so technically speaking they run in parallel.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(ManualUnparkBench.ITERATIONS)
@SuppressWarnings("unused")
public class ManualUnparkBench {

    public static final int ITERATIONS = 100000;

    @Param({"PLATFORM", "VIRTUAL"})
    private ThreadType threadType;
    @Param({"1"})
    private float multiplier;

    private Thread producerThread;
    private Thread consumerThread;
    private Long item;

    @Setup(Level.Invocation)
    public void beforeInvocation() {
        consumerThread = null;
        item = null;

        producerThread = threadType.getThreadBuilder().unstarted(() -> {
            var x = 0L;
            var y = 1L;
            while (true) {
                while (item != null) {
                    LockSupport.park();
                    if (Thread.interrupted()) return;
                }
                item = x;
                var z = x + y;
                //noinspection SuspiciousNameCombination
                x = y;
                y = z;
                LockSupport.unpark(consumerThread);
            }
        });
    }

    @Benchmark
    public void each(final Blackhole bh) throws InterruptedException {
        consumerThread = threadType.getThreadBuilder().start(() -> {
            for (var i = getIterations(); i > 0; i--) {
                while (item == null) {
                    LockSupport.park();
                    if (Thread.interrupted()) return;
                }
                bh.consume(item.longValue());
                item = null;
                LockSupport.unpark(producerThread);
            }
        });
        producerThread.start();
        consumerThread.join();
    }

    @Benchmark
    public void sum(final Blackhole bh) throws InterruptedException {
        consumerThread = threadType.getThreadBuilder().start(() -> {
            var sum = 0L;
            for (var i = getIterations(); i > 0; i--) {
                while (item == null) {
                    LockSupport.park();
                    if (Thread.interrupted()) return;
                }
                sum += item;
                item = null;
                LockSupport.unpark(producerThread);
            }
            UtilsKt.checkFibSum(getIterations(), sum);
            bh.consume(sum);
        });
        producerThread.start();
        consumerThread.join();
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
