package me.broot.benchmark.fibgen;

import me.broot.benchmark.fibgen.util.ThreadType;
import me.broot.benchmark.fibgen.util.UtilsKt;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.SynchronousQueue;

/**
 * Benchmark using `SynchronousQueue`.
 * <p>
 * We start two threads, then use <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/SynchronousQueue.html">SynchronousQueue</a>
 * to pass items between them. Producer and consumer run concurrently. That means if they take more time to finish,
 * then at the time the consumer processes the current item, producer already produces the next one.
 * <p>
 * As `SynchronousQueue` spins for some time before parking the thread, and because both the producer and the consumer
 * are very quick, it is expected the benchmark to provide numbers closer to spinning than parking/unparking.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(SynchronousQueueBench.ITERATIONS)
@SuppressWarnings({"unused", "CatchMayIgnoreException"})
public class SynchronousQueueBench {

    public static final int ITERATIONS = 1000000;

    @Param({"PLATFORM", "VIRTUAL"})
    private ThreadType threadType;
    @Param({"1"})
    private float multiplier;

    private SynchronousQueue<Long> queue;
    private Thread producerThread;

    @Setup(Level.Invocation)
    public void beforeInvocation() {
        queue = new SynchronousQueue<>();

        producerThread = threadType.getThreadBuilder().start(() -> {
            try {
                var x = 0L;
                var y = 1L;
                //noinspection InfiniteLoopStatement
                while (true) {
                    queue.put(x);
                    var z = x + y;
                    //noinspection SuspiciousNameCombination
                    x = y;
                    y = z;
                }
            } catch (InterruptedException e) {}
        });
    }

    @Benchmark
    public void each(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            try {
                for (var i = getIterations(); i > 0; i--) {
                    bh.consume(queue.take().longValue());
                }
            } catch (InterruptedException e) {}
        }).join();
    }

    @Benchmark
    public void sum(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            try {
                var sum = 0L;
                for (var i = getIterations(); i > 0; i--) {
                    sum += queue.take();
                }
                UtilsKt.checkFibSum(getIterations(), sum);
                bh.consume(sum);
            } catch (InterruptedException e) {}
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
