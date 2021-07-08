package fpinkotlin.chap04_recursions

import java.util.concurrent.ConcurrentHashMap

class Memoizer<T, U> private constructor() {
    private val cache = ConcurrentHashMap<T, U>()

    private fun doMemoize(func: (T) -> U): (T) -> U =
        { input ->
            cache.computeIfAbsent(input) {
                func(it)
            }
        }

    companion object {
        fun <T, U> memoize(func: (T) -> U): (T) -> U =
            Memoizer<T, U>().doMemoize(func)
    }
}

fun longComputation(number: Int): Int {
    Thread.sleep(1000L)
    return number
}

fun main(args: Array<String>) {
    val startTime1 = System.currentTimeMillis()
    val result1 = longComputation(43)
    val time1 = System.currentTimeMillis() - startTime1
    val memoizedLongComputation =
        Memoizer.memoize(::longComputation)
    val startTime2 = System.currentTimeMillis()
    val result2 = memoizedLongComputation(43)
    val time2 = System.currentTimeMillis() - startTime2
    val startTime3 = System.currentTimeMillis()
    val result3 = memoizedLongComputation(43)
    val time3 = System.currentTimeMillis() - startTime3

    println("Call to nonmemoized function: result = $result1, time = $time1")
    println("First call to memoized function: result = $result2, time = $time2")
    println("Second call to memoized function: result = $result3, time = $time3")
}

val mhc = Memoizer.memoize { x: Int ->
    Memoizer.memoize { y: Int ->
        x + y
    }
}

val f3 = { x: Int -> { y: Int -> { z: Int -> x + y - z } } }

