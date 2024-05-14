package me.broot.benchmark.fibgen;

import me.broot.benchmark.fibgen.util.UtilsKt;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Iterator;

/**
 * Single-threaded benchmark using an `Iterator`.
 * <p>
 * This benchmark is used as a baseline. It is single-threaded, it calls the producer by the consumer and is potentially
 * the fastest possible implementation. We could use it to estimate the cost associated with running both sides
 * independently.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(IteratorBench.ITERATIONS)
@SuppressWarnings("unused")
public class IteratorBench {

    public static final int ITERATIONS = 1000000000;

    @Param({"1"})
    private float multiplier;

    @SuppressWarnings("SuspiciousNameCombination")
    private static Iterator<Long> createProducer() {
        return new Iterator<>() {
            private long x = 0;
            private long y = 1;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Long next() {
                var oldX = x;
                x = y;
                y += oldX;
                return oldX;
            }
        };
    }

    @Benchmark
    public void each(final Blackhole bh) {
        final var producer = createProducer();
        for (var i = getIterations(); i > 0; i--) {
            bh.consume(producer.next().longValue());
        }
    }

    @Benchmark
    public long sum() {
        final var producer = createProducer();
        var sum = 0L;
        for (var i = getIterations(); i > 0; i--) {
            sum += producer.next();
        }
        UtilsKt.checkFibSum(getIterations(), sum);
        return sum;
    }

    private int getIterations() {
        return (int) (ITERATIONS * multiplier);
    }
}
