package fpinkotlin.chap05_lists

open class Pair<A, B>(val fst: A, val snd: B)

sealed class IList<out A> {

    abstract fun isEmpty(): Boolean

    internal object Nil : IList<Nothing>() {
        override fun isEmpty(): Boolean = true
        override fun toString(): String = "[Nil]"
    }
    internal class Cons<A>(internal val head: A, internal val tail: IList<A>) : IList<A>() {
        override fun isEmpty(): Boolean = false
        override fun toString(): String = "[${toString("", this)}NIL]"

        private tailrec fun toString(acc: String, list: IList<A>): String = when (list) {
            is Nil -> acc
            is Cons -> toString("$acc${list.head} -> ", list.tail)
        }

    }

    fun cons(a: @UnsafeVariance A): IList<A> = Cons(a, this)

    fun setHead(a: @UnsafeVariance A): IList<A> = when (this) {
        is Nil -> throw IllegalStateException("setHead called on an empty list")
        is Cons -> tail.cons(a)
    }

    fun drop(n: Int): IList<A> = Companion.drop(this, n)

    fun dropWhile(p: (A) -> Boolean): IList<A> = Companion.dropWhile(this, p)

    fun concat(list: IList<@UnsafeVariance A>): IList<A> = Companion.concatViaFoldLeft(this, list)

//    fun reverse(): IList<A> = reverse(this, IList.invoke())

    fun init(): IList<A> = when (this) {
        is Nil -> throw IllegalStateException("init called on an empty list")
        is Cons -> reverse().drop(1).reverse()
    }

    fun <B> foldRight(identity: B, f: (A) -> (B) -> B): B = Companion.foldRight(this, identity, f)

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = Companion.foldLeft(identity, this, f)

    fun reverse(): IList<A> =
        foldLeft(Nil as IList<A>) { acc -> { acc.cons(it) } }

    fun <B> foldRightViaFoldLeft(identity: B, f: (A) -> (B) -> B): B =
        this.reverse().foldLeft(identity) { x -> { y -> f(y)(x) } }

    fun <B> coFoldRight(identity: B, f: (A) -> (B) -> B): B =
        coFoldRight(identity, this.reverse(), identity, f)

    companion object {
        operator fun <A> invoke(vararg az: A): IList<A> =
            az.foldRight(Nil as IList<A>) { a: A, list: IList<A> ->
                Cons(a, list)
            }

        tailrec fun <A> drop(list: IList<A>, n: Int): IList<A> = when (list) {
            is Nil -> list
            is Cons -> if (n <= 0) list else drop(list.tail, n - 1)
        }

        tailrec fun <A> dropWhile(list: IList<A>, p: (A) -> Boolean): IList<A> = when (list) {
            is Nil -> list
            is Cons -> if (p(list.head)) dropWhile(list.tail, p) else list
        }

//        fun <A> concat(list1: IList<A>, list2: IList<A>): IList<A> = when (list1) {
//            is Nil -> list2
//            is Cons -> concat(list1.tail, list2).cons(list1.head)
//        }

        fun <A> reverse(list: IList<A>, acc: IList<A>): IList<A> = when (list) {
            is Nil -> list
            is Cons -> reverse(list.tail, acc.cons(list.head))
        }

        fun <A, B> foldRight(list: IList<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                is Nil -> identity
                is Cons -> f(list.head)(foldRight((list.tail), identity, f))
            }

        tailrec fun <A, B> foldLeft(accumulator: B, list: IList<A>, f: (B) -> (A) -> B): B =
            when (list) {
                is Nil -> accumulator
                is Cons -> foldLeft(f(accumulator)(list.head), list.tail, f)
            }

        private tailrec fun <A, B> coFoldRight(acc: B, list: IList<A>, identity: B, f: (A) -> (B) -> B): B =
            when (list) {
                is Nil -> acc
                is Cons -> coFoldRight(f(list.head)(acc), list.tail, identity, f)
            }

        fun <A> concatViaFoldRight(list1: IList<A>, list2: IList<A>) =
            foldRight(list1, list2) { elem -> { acc -> Cons(elem, acc) } }

        fun <A> concatViaFoldLeft(list1: IList<A>, list2: IList<A>) =
            list1.reverse().foldLeft(list2) { x -> x::cons }

    }
}

//fun sum(ints: IList<Int>): Int = when (ints) {
//    is IList.Cons -> ints.head + sum(ints.tail)
//    is IList.Nil -> 0
//}
//
//fun product(ints: IList<Double>): Double = when (ints) {
//    is IList.Cons -> if (ints.head == 0.0)
//                            0.0
//                        else
//                            ints.head * product(ints.tail)
//    is IList.Nil -> 1.0
//}

// Stack-unsafe foldRight versions

//fun sum(list: IList<Int>): Int = IList.foldRight(list, 0) { x -> { y -> x + y }}
//fun product(list: IList<Double>): Double = IList.foldRight(list, 1.0) { x -> { y -> x * y } }
//
//fun <A> IList<A>.size() = this.foldRight(0) { _ -> { it + 1 }}

// Stack-safe foldLeft versions

fun sum(list: IList<Int>): Int = IList.foldLeft(0, list) { x -> { y -> x + y } }
fun product(list: IList<Double>): Double = IList.foldLeft(1.0, list) { x -> { y -> x * y } }
fun <A> IList<A>.size() = this.foldLeft(0) { i -> { i + 1 } }

fun <T> reverse(list: IList<T>): IList<T> =
    IList.foldLeft(IList.invoke(), list) { acc -> { acc.cons(it) } }

fun <A> IList<IList<A>>.flatten(): IList<A> =
    this.coFoldRight(IList.Nil) { x -> x::concat }

// Mapping and filtering

fun triple(list: IList<Int>): IList<Int> =
    list.coFoldRight(IList.invoke()) { x -> { acc -> acc.cons(x * 3) } }

fun doubleToString(list: IList<Double>): IList<String> =
    list.coFoldRight(IList.invoke()) { i -> { acc -> acc.cons(i.toString()) }}

fun <A, B> IList<A>.map(f: (A) -> B): IList<B> =
    this.coFoldRight(IList.invoke()) { x -> { acc -> acc.cons(f(x)) } }

fun <A> IList<A>.filter(p: (A) -> Boolean): IList<A> =
    this.coFoldRight(IList.invoke()) { x -> { acc ->
        if (p(x)) acc.cons(x)
        else acc
    }}

fun <A, B> IList<A>.flatMap(f: (A) -> IList<B>): IList<B> =
    this.map(f).flatten()

fun <A> IList<A>.filterViaFlatMap(p: (A) -> Boolean): IList<A> =
    this.flatMap { if (p(it)) IList(it) else IList.Nil }

fun main(args: Array<String>) {
    println(IList.foldRight(IList(1, 2, 3), IList()) { x: Int ->
        { y: IList<Int> ->
            y.cons(x)
        }
    })
}