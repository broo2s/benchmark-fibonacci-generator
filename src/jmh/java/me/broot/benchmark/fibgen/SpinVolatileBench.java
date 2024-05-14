package me.broot.benchmark.fibgen;

import me.broot.benchmark.fibgen.util.ThreadType;
import me.broot.benchmark.fibgen.util.UtilsKt;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Benchmark using a busy-waiting.
 * <p>
 * We start two threads, then we keep them active by spinning whenever we have to wait for another side. We still
 * alternate between the producer and the consumer doing real work, but technically, they are both active all the time.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(SpinVolatileBench.ITERATIONS)
@SuppressWarnings("unused")
public class SpinVolatileBench {

    public static final int ITERATIONS = 1000000;

    @Param({"PLATFORM", "VIRTUAL"})
    private ThreadType threadType;
    @Param({"1"})
    private float multiplier;

    private Thread producerThread;
    private volatile Long item;

    @Setup(Level.Invocation)
    @SuppressWarnings("SuspiciousNameCombination")
    public void beforeInvocation() {
        item = null;

        producerThread = threadType.getThreadBuilder().start(() -> {
            var x = 0L;
            var y = 1L;
            while (true) {
                while (item != null) {
                    if (Thread.interrupted()) return;
                }
                var oldX = x;
                x = y;
                y += oldX;
                item = oldX;
            }
        });
    }

    @Benchmark
    public void each(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            for (var i = getIterations(); i > 0; i--) {
                while (item == null) {
                    if (Thread.interrupted()) return;
                }
                bh.consume(item.longValue());
                item = null;
            }
        }).join();
    }

    @Benchmark
    public void sum(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            var sum = 0L;
            for (var i = getIterations(); i > 0; i--) {
                while (item == null) {
                    if (Thread.interrupted()) return;
                }
                sum += item;
                item = null;
            }
            UtilsKt.checkFibSum(getIterations(), sum);
            bh.consume(sum);
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
