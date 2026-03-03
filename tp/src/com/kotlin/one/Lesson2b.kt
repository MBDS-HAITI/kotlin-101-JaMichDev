package com.android.one

fun ex1CreateImmutableList(): List<Int> {
    return listOf(1, 2, 3, 4, 5)
}

fun ex2CreateMutableList(): List<String> {
    val values = mutableListOf("Kotlin", "Java", "Swift")
    values.add("Dart")
    return values
}

fun ex3FilterEvenNumbers(): List<Int> {
    return (1..10).filter { it % 2 == 0 }
}

fun ex4FilterAndMapAges(): List<String> {
    val ages = listOf(12, 17, 18, 21, 30)
    return ages
        .filter { it >= 18 }
        .map { "Adult: $it" }
}

fun ex5FlattenList(): List<Int> {
    val nested = listOf(listOf(1, 2), listOf(3, 4), listOf(5))
    return nested.flatten()
}

fun ex6FlatMapWords(): List<String> {
    val phrases = listOf("Kotlin is fun", "I love lists")
    return phrases.flatMap { it.split(" ") }
}

fun ex7EagerProcessing(): List<Int> {
    val start = System.currentTimeMillis()
    val result = (1..1_000_000).toList()
        .filter { it % 3 == 0 }
        .map { it * it }
        .take(5)
    val end = System.currentTimeMillis()
    println("ex7 eager Time: ${end - start} ms")
    return result
}

fun ex8LazyProcessing(): List<Int> {
    val start = System.currentTimeMillis()
    val result = (1..1_000_000).asSequence()
        .filter { it % 3 == 0 }
        .map { it * it }
        .take(5)
        .toList()
    val end = System.currentTimeMillis()
    println("ex8 lazy Time: ${end - start} ms")
    return result
}

fun ex9FilterAndSortNames(): List<String> {
    val names = listOf("alice", "Bob", "adam", "Clara", "amelia", "alex")
    return names
        .filter { it.startsWith("a", ignoreCase = true) }
        .map { it.uppercase() }
        .sorted()
}

fun runtest(name: String, test: () -> Boolean): Boolean {
    return try {
        check(test())
        println("✅ $name")
        true
    } catch (e: Throwable) {
        println("❌ $name")
        false
    }
}

fun main() {
    var passed = 0
    var failed = 0

    fun run(name: String, test: () -> Boolean) {
        if (runtest(name, test)) passed++ else failed++
    }

    run("ex1 create ImmutableList") { ex1CreateImmutableList() == listOf(1, 2, 3, 4, 5) }
    run("ex2 create MutableList") { ex2CreateMutableList() == listOf("Kotlin", "Java", "Swift", "Dart") }
    run("ex3 filter EvenNumbers") { ex3FilterEvenNumbers() == listOf(2, 4, 6, 8, 10) }
    run("ex4 filter AndMapAges") { ex4FilterAndMapAges() == listOf("Adult: 18", "Adult: 21", "Adult: 30") }
    run("ex5 flatten List") { ex5FlattenList() == listOf(1, 2, 3, 4, 5) }
    run("ex6 flat MapWords") { ex6FlatMapWords() == listOf("Kotlin", "is", "fun", "I", "love", "lists") }
    run("ex7 eager Processing") { ex7EagerProcessing() == listOf(9, 36, 81, 144, 225) }
    run("ex8 lazy Processing") { ex8LazyProcessing() == listOf(9, 36, 81, 144, 225) }
    run("ex9 filter AndSortNames") { ex9FilterAndSortNames() == listOf("ADAM", "ALEX", "ALICE", "AMELIA") }

    println("\n🎯 TEST SUMMARY: $passed passed, $failed failed")
}