package fpinkotlin.chap08_advanced_lists

import fpinkotlin.chap05_lists.IList
import fpinkotlin.chap05_lists.flatten
import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap07_exceptions.map2

sealed class MList<out A> {

    abstract fun isEmpty(): Boolean

    abstract val length: Int

    abstract fun forEach(ef: (A) -> Unit) // Chap 12.

    internal object Nil : MList<Nothing>() {
        override fun isEmpty(): Boolean = true
        override fun toString(): String = "[Nil]"

        override val length: Int = 0

        override fun forEach(ef: (Nothing) -> Unit) {}
    }
    internal class Cons<A>(internal val head: A, internal val tail: MList<A>) : MList<A>() {
        override fun isEmpty(): Boolean = false
        override fun toString(): String = "[${toString("", this)}NIL]"

        private tailrec fun toString(acc: String, list: MList<A>): String = when (list) {
            is Nil -> acc
            is Cons -> toString("$acc${list.head} -> ", list.tail)
        }

        override val length: Int = tail.length + 1

        override fun forEach(ef: (A) -> Unit) { // Chap. 12
            tailrec fun forEach(list: MList<A>) {
                when (list) {
                    is Nil -> {}
                    is Cons -> {
                        ef(list.head)
                        forEach(list.tail)
                    }
                }
            }
            forEach(this)
        }
    }

    fun cons(a: @UnsafeVariance A): MList<A> = Cons(a, this)

    fun setHead(a: @UnsafeVariance A): MList<A> = when (this) {
        is Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }

    fun headSafe(): Result<A> = when (this) {
        is Nil -> Result()
        is Cons -> Result(this.head)
    }

    fun lastSafe(): Result<A> = foldLeft(Result()) { _: Result<A> -> { y: A -> Result(y) } }

    fun drop(n: Int): MList<A> = Companion.drop(this, n)

    fun dropWhile(p: (A) -> Boolean): MList<A> = Companion.dropWhile(this, p)

    fun concat(list: MList<@UnsafeVariance A>): MList<A> = Companion.concatViaFoldLeft(this, list)

    fun init(): MList<A> = when (this) {
        is Nil -> throw IllegalStateException("init called on an empty list")
        is Cons -> reverse().drop(1).reverse()
    }

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B = Companion.foldRight(this, identity, f)

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = Companion.foldLeft(identity, this, f)

    fun reverse(): MList<A> =
        foldLeft(Nil as MList<A>) { acc -> { acc.cons(it) } }

    fun <B> foldRightViaFoldLeft(identity: B, f: (A) -> (B) -> B): B =
        this.reverse().foldLeft(identity) { x -> { y -> f(y)(x) } }

    fun <B> coFoldRight(identity: B, f: (A) -> (B) -> B): B =
        coFoldRight(identity, this.reverse(), identity, f)

    companion object {
        operator fun <A> invoke(vararg az: A): MList<A> =
            az.foldRight(Nil as MList<A>) { a: A, list: MList<A> ->
                Cons(a, list)
            }

        tailrec fun <A> drop(list: MList<A>, n: Int): MList<A> = when (list) {
            is Nil -> list
            is Cons -> if (n <= 0) list else drop(list.tail, n - 1)
        }

        tailrec fun <A> dropWhile(list: MList<A>, p: (A) -> Boolean): MList<A> = when (list) {
            is Nil -> list
            is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
        }

        fun <A> reverse(list: MList<A>, acc: MList<A>): MList<A> = when (list) {
            is Nil -> list
            is Cons -> reverse(list.tail, acc.cons(list.head))
        }

        fun <A, B> foldRight(list: MList<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                is Nil -> identity
                is Cons -> f(list.head)(foldRight((list.tail), identity, f))
            }

        tailrec fun <A, B> foldLeft(accumulator: B, list: MList<A>, f: (B) -> (A) -> B): B =
            when (list) {
                is Nil -> accumulator
                is Cons -> foldLeft(f(accumulator)(list.head), list.tail, f)
            }

        private tailrec fun <A, B> coFoldRight(acc: B, list: MList<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                is Nil -> acc
                is Cons -> coFoldRight(f(list.head)(acc), list.tail, identity, f)
            }

        fun <A> concatViaFoldRight(list1: MList<A>, list2: MList<A>) =
            foldRight(list1, list2) { elem -> { acc -> Cons(elem, acc) } }

        fun <A> concatViaFoldLeft(list1: MList<A>, list2: MList<A>) =
            list1.reverse().foldLeft(list2) { x -> x::cons }

    }
}

fun <A> MList<MList<A>>.flatten(): MList<A> =
    this.coFoldRight(MList.Nil) { x -> x::concat }

fun <A, B> MList<A>.map(f: (A) -> B): MList<B> =
    this.foldLeft<MList<B>>(MList.invoke()) { acc -> { x -> acc.cons(f(x)) } }.reverse()
    // Look like implementation of coFoldRight not working...

fun <A, B> MList<A>.flatMap(f: (A) -> MList<B>): MList<B> =
    this.map(f).flatten()

fun <A> MList<A>.filter(p: (A) -> Boolean): MList<A> =
    this.flatMap { if (p(it)) MList(it) else MList.Nil }

fun <A> flattenResult(list: MList<Result<A>>): MList<A> =
    list.flatMap { res -> res.map { MList(it) }.getOrElse(MList()) }

fun <A> sequence(list: MList<Result<A>>): Result<MList<A>> =
    list.coFoldRight(Result(MList())) { x ->
        { y ->
            map2(x, y) { a -> { b: MList<A> -> b.cons(a) } }
        }
    }

fun <A> sequence2(list: MList<Result<A>>): Result<MList<A>> =
    list.filter { !it.isEmpty() }.coFoldRight(Result(MList())) { x ->
        { y ->
            map2(x, y) { a -> { b: MList<A> -> b.cons(a) } }
        }
    }

fun <A, B> traverse(list: MList<A>, f: (A) -> Result<B>): Result<MList<B>> =
    list.coFoldRight(Result(MList())) { x ->
        { y ->
            map2(f(x), y) { a -> { b: MList<B> -> b.cons(a) } }
        }
    }

fun <A> sequenceViaTraverse(list: MList<Result<A>>): Result<MList<A>> =
    traverse(list) { x -> x }

fun main() {
    val m = MList(1, 2, 3, 4, 5)
    m.map { it * it }.map {
        println(it)
    }

    val g = MList(MList(1, 2, 3), MList(4, 5), MList())

    g.flatten().map {
        println(it)
    }
}