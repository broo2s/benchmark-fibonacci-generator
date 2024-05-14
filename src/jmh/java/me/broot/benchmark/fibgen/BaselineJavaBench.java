package me.broot.benchmark.fibgen;

import me.broot.benchmark.fibgen.util.ThreadType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@OperationsPerInvocation(BaselineJavaBench.ITERATIONS)
@SuppressWarnings("unused")
public class BaselineJavaBench {

    public static final int ITERATIONS = 1000000000;

    @Param({"VIRTUAL"})
    private ThreadType threadType;
    @Param({"1"})
    private float multiplier;

    private long result;

    @Benchmark
    public void each(final Blackhole bh) {
        for (var i = getIterations(); i > 0; i--) {
            bh.consume((long) i);
        }
    }

    @Benchmark
    public void eachInThread(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            for (var i = getIterations(); i > 0; i--) {
                bh.consume((long) i);
            }
        }).join();
    }

    @Benchmark
    public long sum() {
        var sum = 0L;
        for (var i = getIterations(); i > 0; i--) {
            sum += i;
        }
        return sum;
    }

    @Benchmark
    public void sumInThread(final Blackhole bh) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            var sum = 0L;
            for (var i = getIterations(); i > 0; i--) {
                sum += i;
            }
            bh.consume(sum);
        }).join();
    }

    @Benchmark
    public void sumInThreadWithLocals(final Blackhole bh1) throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            @SuppressWarnings("UnnecessaryLocalVariable")
            final var bh = bh1;
            var sum = 0L;
            for (var i = getIterations(); i > 0; i--) {
                sum += i;
            }
            bh.consume(sum);
        }).join();
    }

    @Benchmark
    public long sumInThreadWithResultField() throws InterruptedException {
        threadType.getThreadBuilder().start(() -> {
            var sum = 0L;
            for (var i = getIterations(); i > 0; i--) {
                sum += i;
            }
            result = sum;
        }).join();
        return result;
    }

    private int getIterations() {
        return (int) (ITERATIONS * multiplier);
    }
}

