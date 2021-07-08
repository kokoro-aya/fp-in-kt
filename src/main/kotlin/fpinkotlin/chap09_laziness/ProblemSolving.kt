package fpinkotlin.chap09_laziness

import fpinkotlin.chap07_exceptions.Result
import java.math.BigInteger

val fibs: Stream<BigInteger>
    get() = Stream.iterate(BigInteger.ONE to BigInteger.ONE) { (x, y) -> y to (x + y) }.map { it.first }

val fibsUnfold: Stream<BigInteger>
    get() = Stream.unfold(BigInteger.ONE to BigInteger.ONE) { (x, y) ->
        Result(x to (y to (x + y)))
    }

fun <A> Stream<A>.filter(p: (A) -> Boolean): Stream<A> =
    dropWhile { !p(it) }.let { stream ->
        when (stream) {
            is Stream.Empty -> stream
            is Stream.Cons -> Stream.cons(stream.hd, Lazy { stream.tl().filter(p) })
        }
    } // Stack-safe version of filter()


fun main() {
    println(fibs.takeAtMost(100).toList().toString())
    println(fibsUnfold.takeAtMost(100).toList().toString())
}