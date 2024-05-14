package me.broot.benchmark.fibgen

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import me.broot.benchmark.fibgen.util.checkFibSum
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

private const val ITERATIONS: Int = 10000000

/**
 * Benchmark using a Kotlin `Flow`.
 *
 * It uses the [flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/flow.html)
 * builder to emit one item at a time.
 *
 * This benchmark doesn't meet requirements of running the producer and the consumer in separate execution contexts.
 * Consumer calls the producer passing a callback and the producer calls this callback. Consumer doesn't process items
 * in its own loop.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(ITERATIONS)
@Suppress("unused")
open class FlowBench {

    @Param("1")
    private var multiplier = 0f

    private val producer get() = flow {
        var x = 0L
        var y = 1L
        while (true) {
            emit(x)
            val z = x + y
            x = y
            y = z
        }
    }

    @Benchmark
    fun each(bh: Blackhole) {
        runBlocking {
            producer.take(iterations).collect { bh.consume(it) }
        }
    }

    @Benchmark
    fun sum(): Long {
        val result = runBlocking {
            producer.take(iterations).reduce(Long::plus)
        }
        checkFibSum(iterations, result)
        return result
    }

    private val iterations get() = (ITERATIONS * multiplier).toInt()
}