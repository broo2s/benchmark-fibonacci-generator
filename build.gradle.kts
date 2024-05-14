plugins {
    java
    kotlin("jvm") version "1.9.23"
    id("me.champeau.jmh") version "0.7.2"
}

group = "me.broot"
version = "1.0-SNAPSHOT"

jmh {
    fork = 1
    warmupIterations = 2
    warmup = "2s"
    iterations = 5
    timeOnIteration = "2s"
    timeUnit = "ns"
    benchmarkMode = listOf("avgt")

    excludes.add("InThread")
    excludes.add("InCoroutine")

    // uncomment to make sure all benchmarks take >100ms per invocation
//    benchmarkMode = listOf("ss")
//    operationsPerInvocation = 1
//    timeUnit = "ms"

//    benchmarkParameters.put("multiplier", listProperty("0.01", "0.1", "1"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

inline fun <reified T> listProperty(vararg items: T): ListProperty<T> = objects.listProperty<T>().apply { addAll(items.asIterable()) }
