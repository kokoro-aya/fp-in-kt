package fpinkotlin.chap04_recursions

import java.math.BigInteger

fun fibonacci(n: Int): BigInteger {
    tailrec fun fibonacci_(a: BigInteger, b: BigInteger, i: Int): BigInteger =
        when {
            i < 0 -> throw IllegalArgumentException()
            i == 0 -> a
            else -> fibonacci_(b, a + b, i - 1)
        }
    return fibonacci_(BigInteger.ZERO, BigInteger.ONE, n)
}

fun main(args: Array<String>) {
    (1 .. 20).forEach { println(fibonacci(it)) }
}