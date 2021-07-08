package fpinkotlin.chap04_recursions

import java.math.BigInteger
import java.math.BigInteger.ONE
import java.math.BigInteger.ZERO

fun fibo(number: Int): String {
    tailrec fun fibo_(
        storage: List<BigInteger>,
        left: BigInteger, right: BigInteger, i: Int): List<BigInteger> = when (i) {
            0 -> storage
            1 -> storage + (left + right)
            else -> fibo_(storage + (left + right), right, left + right, i - 1)
        }
    return makeString(fibo_(listOf(), ZERO, ONE, number), ", ")
}

fun <T> iterate(seed: T, f: (T) -> T, n: Int): List<T> {
    tailrec fun iterate_(acc: List<T>, seed: T): List<T> =
        if (acc.size < n)
            iterate_(acc + seed, f(seed))
        else
            acc
    return iterate_(listOf(), seed)
}

fun <T, U> map(list: List<T>, action: (T) -> U): List<U> =
    foldLeft(list, listOf()) {
        e, f -> e + action(f)
    }

fun fibo2(number: Int): String {
    val seed = Pair(BigInteger.ZERO, BigInteger.ONE)
    val f = { x: Pair<BigInteger, BigInteger> ->
        Pair(x.second, x.first + x.second)
    }
    val listOfPairs = iterate(seed, f, number + 1)
    val list = map(listOfPairs) { p -> p.first }
    return makeString(list, ", ")
}