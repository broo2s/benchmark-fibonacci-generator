package me.broot.benchmark.fibgen

import me.broot.benchmark.fibgen.util.checkFibSum
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.intrinsics.*
import kotlin.coroutines.resume

private const val ITERATIONS: Int = 100000000

/**
 * Benchmark using a manual `Continuation` passing.
 *
 * We use the low-level API of Kotlin coroutines to manually suspend, resume and pass [continuations](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-continuation/).
 *
 * Benchmark is expected to run in a single thread, but alternating between the producer and the consumer.
 */
@State(Scope.Benchmark)
@OperationsPerInvocation(ITERATIONS)
@Suppress("unused")
open class ContinuationsBench {

    @Param("1")
    private var multiplier = 0f

    private var item: Long = -1
    private lateinit var producerContinuation: Continuation<Unit>

    @Setup(Level.Invocation)
    fun beforeInvocation() {
        producerContinuation = suspend {
            var x = 0L
            var y = 1L
            while (true) {
                suspendCoroutineUninterceptedOrReturn {
                    producerContinuation = it
                    item = x
                    COROUTINE_SUSPENDED
                }
                val z = x + y
                x = y
                y = z
            }
        }.createCoroutineUnintercepted(Continuation(EmptyCoroutineContext) {})
    }

    @Benchmark
    fun each(bh: Blackhole) {
        repeat(iterations) {
            producerContinuation.resume(Unit)
            bh.consume(item)
        }
    }

    @Benchmark
    fun eachWithLoop(bh: Blackhole) {
        var i = iterations
        while (i > 0) {
            producerContinuation.resume(Unit)
            bh.consume(item)
            i--
        }
    }

    @Benchmark
    fun sum(): Long {
        var sum = 0L
        repeat(iterations) {
            producerContinuation.resume(Unit)
            sum += item
        }

        checkFibSum(iterations, sum)
        return sum
    }

    @Benchmark
    fun sumWithLoop(): Long {
        var sum = 0L
        var i = iterations
        while (i > 0) {
            producerContinuation.resume(Unit)
            sum += item
            i--
        }

        checkFibSum(iterations, sum)
        return sum
    }

    private val iterations get() = (ITERATIONS * multiplier).toInt()
}