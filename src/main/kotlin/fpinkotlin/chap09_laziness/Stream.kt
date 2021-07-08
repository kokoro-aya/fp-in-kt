package fpinkotlin.chap09_laziness

import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap08_advanced_lists.MList
import fpinkotlin.chap08_advanced_lists.filter
import fpinkotlin.chap08_advanced_lists.map

sealed class Stream<out A> {
    abstract fun isEmpty(): Boolean
    abstract fun head(): Result<A>
    abstract fun tail(): Result<Stream<A>>

    abstract fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B

    internal object Empty: Stream<Nothing>() {
        override fun head(): Result<Nothing> = Result()
        override fun tail(): Result<Nothing> = Result()
        override fun isEmpty(): Boolean = true

        override fun <B> foldRight(z: Lazy<B>, f: (Nothing) -> (Lazy<B>) -> B): B = z()
    }

    internal class Cons<out A>(internal val hd: Lazy<A>, internal val tl: Lazy<Stream<A>>): Stream<A>() {
        override fun head(): Result<A> = Result(hd())
        override fun tail(): Result<Stream<A>> = Result(tl())
        override fun isEmpty(): Boolean = false

        override fun <B> foldRight(z: Lazy<B>, f: (A) -> (Lazy<B>) -> B): B =
            f(this.hd())(Lazy { this.tl().foldRight(z, f) })
    }

    companion object {
        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> = Cons(hd, tl)

        operator fun <A> invoke(): Stream<A> = Empty

//        fun from(i: Int): Stream<Int> =
//            cons(Lazy { i }, Lazy { from(i + 1) })

        private tailrec fun <A> dropAtMost(n: Int, stream: Stream<A>): Stream<A> = when {
            n > 0 -> when (stream) {
                is Empty -> stream
                is Cons -> dropAtMost(n - 1, stream.tl())
            }
            else -> stream
        }

        private tailrec fun <A> dropWhile(p: (A) -> Boolean, stream: Stream<A>): Stream<A> = when (stream) {
            is Empty -> stream
            is Cons -> when {
                p(stream.hd()) -> dropWhile(p, stream.tl())
                else -> stream
            }
        }

        private tailrec fun <A> exists(stream: Stream<A>, p: (A) -> Boolean): Boolean = when (stream) {
            is Empty -> false
            is Cons -> when {
                p(stream.hd()) -> true
                else -> exists(stream.tl(), p)
            }
        }

        fun <A> iterate(seed: A, f: (A) -> A): Stream<A> =
            cons(Lazy{ seed }, Lazy { iterate(f(seed), f) })

//        fun from(i: Int): Stream<Int> = iterate(i) { it + 1 }

        fun <A, S> unfold(z: S, f: (S) -> Result<Pair<A, S>>): Stream<A> =
            f(z).map { (a, s) ->
                cons(Lazy { a }, Lazy { unfold(s, f) })
            }.getOrElse(Empty)

        fun from(n: Int): Stream<Int> = unfold(n) { x -> Result(x to (x + 1)) }

    }

    fun takeAtMost(n: Int): Stream<A> = when (this) {
        is Empty -> this
        is Cons -> when {
            n > 0 -> cons(hd, Lazy { tl().takeAtMost(n - 1) })
            else -> Empty
        }
    }

//    fun dropAtMost(n: Int): Stream<A> = when (this) {
//        is Empty -> this
//        is Cons -> when {
//            n > 0 -> tl().dropAtMost(n - 1)
//            else -> this
//        }
//    } // This will cause stack overflow

    fun dropAtMost(n: Int): Stream<A> = Companion.dropAtMost(n, this)

    fun toList(): MList<A> {
        tailrec fun toList(list: MList<A>, stream: Stream<A>): MList<A> = when (stream) {
            is Empty -> list
            is Cons -> toList(list.cons(stream.hd()), stream.tl())
        }
        return toList(MList(), this).reverse()
    }

    fun takeWhile(p: (A) -> Boolean): Stream<A> = when (this) {
        is Empty -> this
        is Cons -> when {
            p(hd()) -> cons(hd, Lazy { tl().takeWhile(p) })
            else -> Empty
        }
    }

    fun dropWhile(p: (A) -> Boolean): Stream<A> = Companion.dropWhile(p, this)

    fun exists(p: (A) -> Boolean): Boolean = Companion.exists(this, p)

    fun takeWhileViaFoldRight(p: (A) -> Boolean): Stream<A> =
        foldRight(Lazy { Empty }) { a ->
            { acc: Lazy<Stream<A>> ->
                if (p(a))
                    cons(Lazy { a }, acc)
                else
                    Empty
            }
        }

    fun headSafe(): Result<A> =
        this.foldRight(Lazy { Result<A>() }) { a -> { Result(a) }}

    fun <B> map(f: (A) -> B): Stream<B> =
        this.foldRight(Lazy { Empty }) { a ->
            { acc: Lazy<Stream<B>> ->
                cons(Lazy { f(a) }, acc)
            }
        }

//    fun filter(p: (A) -> Boolean): Stream<A> =
//        this.foldRight(Lazy { Empty }) { a ->
//            { acc: Lazy<Stream<A>> ->
//                if (p(a)) cons(Lazy { a }, acc) else acc()
//            }
//        } // Not stack-safe

    fun append(stream2: Lazy<Stream<@UnsafeVariance A>>): Stream<A> =
        this.foldRight(stream2) { a: A ->
            { b: Lazy<Stream<A>> ->
                cons(Lazy{ a }, b)
            }
        }

    fun <B> flatMap(f: (A) -> Stream<B>): Stream<B> =
        this.foldRight(Lazy { Empty }) { a: A ->
            { acc: Lazy<Stream<B>> ->
                f(a).append(acc)
            }
        }

    fun find(p: (A) -> Boolean): Result<A> = filter(p).headSafe()
}

fun <A> repeat(f: () -> A): Stream<A> =
    Stream.cons(Lazy { f() }, Lazy { repeat(f) })

//fun main() {
//    val stream = Stream.from(1)
//    stream.head().forEachOrElse({ println(it) })
//    stream.tail().flatMap { it.head() }.forEachOrElse({ println(it) })
//    stream.tail().flatMap { it.tail().flatMap { it.head() } }.forEachOrElse({ println(it) })
//}

//fun main() {
//    val stream = Stream.from(0).dropAtMost(60000).takeAtMost(60000)
//    println(stream.toList()) // will blow the stack!
//}

fun inc(i: Int): Int = (i + 1).let {
    println("generating $it")
    it
}

//fun main() {
//    val list = Stream
//        .iterate(0, ::inc)
//        .takeAtMost(60000)
//        .dropAtMost(10000)
//        .takeAtMost(10)
//        .toList()
//
//    println(list)
//}
/*
    ......
    generating 10005
    generating 10006
    generating 10007
    generating 10008
    generating 10009
    generating 10010
    [10000 -> 10001 -> 10002 -> 10003 -> 10004 -> 10005 -> 10006 -> 10007 -> 10008 -> 10009 -> NIL]
 */

fun main() {
    val f = { x: Int ->
        println("Mapping $x")
        x * 3
    }
    val p = { x: Int ->
        println("Filtering $x")
        x % 2 == 0
    }

    val list = MList(1,2,3,4,5).map(f).filter(p)
    println(list)
    println()
    val stream = Stream.from(1).takeAtMost(5).map(f).filter(p)
    println(stream.toList())
}
/*
    Mapping 1
    Mapping 2
    Mapping 3
    Mapping 4
    Mapping 5
    Filtering 3
    Filtering 6
    Filtering 9
    Filtering 12
    Filtering 15
    [6 -> 12 -> NIL]

    Mapping 1
    Filtering 3
    Mapping 2
    Filtering 6
    Mapping 3
    Filtering 9
    Mapping 4
    Filtering 12
    Mapping 5
    Filtering 15
    [6 -> 12 -> NIL]
 */