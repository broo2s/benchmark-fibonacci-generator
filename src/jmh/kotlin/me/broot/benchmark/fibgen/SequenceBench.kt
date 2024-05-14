package me.broot.benchmark.fibgen

import me.broot.benchmark.fibgen.util.checkFibSum
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

private const val ITERATIONS: Int = 100000000

/**
 * Benchmark using the `sequence` generator from Kotlin stdlib.
 *
 * It uses a builtin [sequence](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/sequence.html) generator
 * to yield items one at a time. It is expected that the producer and the consumer run in a single thread, by
 * alternating the execution between them.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(ITERATIONS)
@Suppress("unused")
open class SequenceBench {

    @Param("1")
    private var multiplier = 0f

    private val producer get() = sequence {
        var x = 0L
        var y = 1L
        while (true) {
            yield(x)
            val z = x + y
            x = y
            y = z
        }
    }

    @Benchmark
    fun each(bh: Blackhole) {
        producer.take(iterations).forEach { bh.consume(it) }
    }

    @Benchmark
    fun eachWithLoop(bh: Blackhole) {
        val iter = producer.iterator()
        var i = iterations
        while (i > 0) {
            bh.consume(iter.next())
            i--
        }
    }

    @Benchmark
    fun sum(): Long {
        val result = producer.take(iterations).sum()
        checkFibSum(iterations, result)
        return result
    }

    @Benchmark
    fun sumWithLoop(): Long {
        var sum = 0L
        val iter = producer.iterator()
        var i = iterations
        while (i > 0) {
            sum += iter.next()
            i--
        }
        checkFibSum(iterations, sum)
        return sum
    }

    private val iterations get() = (ITERATIONS * multiplier).toInt()
}
