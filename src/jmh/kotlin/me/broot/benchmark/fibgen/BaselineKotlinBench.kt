package me.broot.benchmark.fibgen

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

private const val ITERATIONS: Int = 1000000000

@State(Scope.Benchmark)
@OperationsPerInvocation(ITERATIONS)
@Suppress("unused")
open class BaselineKotlinBench {
    @Param("1")
    private var multiplier = 0f

    @Benchmark
    fun each(bh: Blackhole) {
        repeat(iterations) {
            bh.consume(it.toLong())
        }
    }

    @Benchmark
    fun eachWithLoop(bh: Blackhole) {
        var i = iterations
        while (i > 0) {
            bh.consume(i.toLong())
            i--
        }
    }

    @Benchmark
    fun sum(): Long {
        var sum = 0L
        repeat(iterations) {
            sum += it
        }
        return sum
    }

    @Benchmark
    fun sumWithLoop(): Long {
        var sum = 0L
        var i = iterations
        while (i > 0) {
            sum += i
            i--
        }
        return sum
    }

    @Benchmark
    fun eachInCoroutine(bh: Blackhole) = runBlocking(Dispatchers.Default) {
        repeat(iterations) {
            bh.consume(it.toLong())
        }
    }

    @Benchmark
    @Suppress("UnnecessaryVariable")
    fun eachInCoroutineWithLocals(bh1: Blackhole) = runBlocking(Dispatchers.Default) {
        val bh = bh1
        repeat(iterations) {
            bh.consume(it.toLong())
        }
    }

    private val iterations: Int
        get() = (ITERATIONS * multiplier).toInt()
}