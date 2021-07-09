package fpinkotlin.chap11_self_balancing_trees

import fpinkotlin.chap06_optionals.Optional
import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap08_advanced_lists.MList

sealed class Heap<out A: Comparable<@UnsafeVariance A>> {
    internal abstract val left: Result<Heap<A>>
    internal abstract val right: Result<Heap<A>>
    internal abstract val head: Result<A>

    protected abstract val rank: Int
    abstract val size: Int
    abstract val isEmpty: Boolean

    abstract fun tail(): Result<Heap<A>>
    abstract operator fun get(index: Int): Result<A>

    abstract fun pop(): Optional<Pair<A, Heap<A>>>

    abstract class Empty<out A: Comparable<@UnsafeVariance A>>: Heap<A>() {
        override val isEmpty: Boolean = true
        override val left: Result<Heap<A>> = Result(E)
        override val right: Result<Heap<A>> = Result(E)
        override val head: Result<A> = Result.failure("head() called on empty heap")
        override val rank: Int = 0
        override val size: Int = 0

        override fun tail(): Result<Heap<A>> = Result.failure(IllegalStateException("tail() called on empty heap"))
        override fun get(index: Int): Result<A> = Result.failure(IllegalStateException("Index out of bounds"))

        override fun pop(): Optional<Pair<A, Heap<A>>> = Optional()
    }

    internal object E: Empty<Nothing>()

    internal class H<out A: Comparable<@UnsafeVariance A>> (
        override val rank: Int, private val _left: Heap<A>, private val _head: A, private val _right: Heap<A>): Heap<A>() {
        override val isEmpty: Boolean = false
        override val left: Result<Heap<A>> = Result(_left)
        override val right: Result<Heap<A>> = Result(_right)
        override val head: Result<A> = Result(_head)
        override val size: Int = _left.size + _right.size + 1

        override fun tail(): Result<Heap<A>> = Result(merge(_left, _right))
        override fun get(index: Int): Result<A> = when (index) {
            0 -> head
            else -> tail().flatMap { it[index - 1] }
        }

        override fun pop(): Optional<Pair<A, Heap<A>>> = Optional(_head to merge(_left, _right))
    }

    companion object {
        operator fun <A: Comparable<A>> invoke(): Heap<A> = E

        operator fun <A: Comparable<A>> invoke(element: A): Heap<A> = H(1, E, element, E)

        protected fun <A: Comparable<A>> merge(head: A, first: Heap<A>, second: Heap<A>): Heap<A> = when {
            first.rank >= second.rank -> H(second.rank + 1, first, head, second)
            else -> H(first.rank + 1, second, head, first)
        }

        fun <A: Comparable<A>> merge(first: Heap<A>, second: Heap<A>): Heap<A> =
            first.head.flatMap { fh ->
                second.head.flatMap { sh ->
                    when {
                        fh <= sh -> first.left.flatMap { fl ->
                            first.right.map { fr ->
                                merge(fh, fl, merge(fr, second))
                            }
                        }
                        else -> second.left.flatMap { sl ->
                            second.right.map { sr ->
                                merge(sh, sl, merge(first, sr))
                            }
                        }
                    }
                }
            }.getOrElse(when (first) {
                E -> second
                else -> first
            })
    }

    operator fun plus(element: @UnsafeVariance A): Heap<A> = merge(this, Heap(element))

    fun <B> foldLeft(identity: B, f: (B) -> (A) -> B): B = unfold(this, identity, { it.pop() }, f)

    fun toList(): MList<A> = foldLeft<MList<A>>(MList()) { list -> { a -> list.cons(a) } }.reverse()
}

fun <A, S, B> unfold(z: S, identity: B, getNext: (S) -> Optional<Pair<A, S>>, transform: (B) -> (A) -> B): B {
    tailrec fun unfold(acc: B, z: S): B {
        val next = getNext(z)
        return when (next) {
            is Optional.None -> acc
            is Optional.Some ->
                unfold(transform(acc)(next.value.first), next.value.second)
        }
    }
    return unfold(identity, z) }

// Non comparable PQ is not implemented due to type failure on merge function

fun main(args: Array<String>) {
    val pq = Heap<Int>() + 7 + 8 + 3 + 2 + 5 + 4 + 1 + 6 + 9
    println(pq.toList().toString())
}