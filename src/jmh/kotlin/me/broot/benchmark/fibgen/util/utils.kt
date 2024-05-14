package me.broot.benchmark.fibgen.util

private val fibSums = mapOf(
    1 to 0L,
    2 to 1L,
    5 to 7L,
    10 to 88L,
    100 to 1298777728820984004L,
    1000 to 9079565065540428012L,
    10000 to -83367563645688772L,
    100000 to -4040291346873926564L,
    1000000 to 2756670985995446684L,
    10000000 to 8644293272739028508L,
    100000000 to -1720855460981609700L,
    1000000000 to -1710041672094094052L,
)

fun checkFibSum(n: Int, sum: Long) {
    val expected = fibSums[n]
    check(sum == expected) { "Invalid result, n=$n, expected=$expected, actual=$sum" }
}
