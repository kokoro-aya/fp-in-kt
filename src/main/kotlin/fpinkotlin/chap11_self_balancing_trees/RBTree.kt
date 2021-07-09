package fpinkotlin.chap11_self_balancing_trees

import fpinkotlin.chap11_self_balancing_trees.Color.*
import fpinkotlin.chap07_exceptions.Result
import kotlin.math.max

sealed class Color {
    internal object Red: Color() {
        override fun toString(): String = "R"
        operator fun not() = Black
    }
    internal object Black: Color() {
        override fun toString(): String = "B"
        operator fun not() = Red
    }
}

sealed class Tree <out A: Comparable<@UnsafeVariance A>> {
    abstract val size: Int
    abstract val height: Int
    abstract val isEmpty: Boolean
    internal abstract val color: Color
    internal abstract val isTB: Boolean
    internal abstract val isTR: Boolean
    internal abstract val right: Tree<A>
    internal abstract val left: Tree<A>
    internal abstract val value: A

    abstract fun <B> foldInReverseOrder(identity: B, f: (B) -> (A) -> (B) -> B): B

    internal abstract class Empty<out A: Comparable<@UnsafeVariance A>>: Tree<A>() {
        override val size: Int = 0
        override val height: Int = -1
        override val isEmpty: Boolean = true
        override val color: Color = Black
        override val isTB: Boolean = false
        override val isTR: Boolean = false
        override val right: Tree<Nothing> by lazy {
            throw IllegalStateException("right called on Empty tree")
        }
        override val left: Tree<Nothing> by lazy {
            throw IllegalStateException("left called on Empty tree")
        }
        override val value: Nothing by lazy {
            throw IllegalStateException("value called on Empty tree")
        }

        override fun toString(): String = "E"

        override fun <B> foldInReverseOrder(identity: B, f: (B) -> (A) -> (B) -> B): B = identity
    }

    internal object E: Empty<Nothing>()

    internal class T<out A: Comparable<@UnsafeVariance A>> (
        override val color: Color,
        override val left: Tree<A>,
        override val value: A,
        override val right: Tree<A>): Tree<A>() {
        override val isTB: Boolean = color == Black
        override val isTR: Boolean = color == Red
        override val size: Int = left.size + 1 + right.size
        override val height: Int = max(left.height, right.height) + 1
        override val isEmpty: Boolean = false
        override fun toString(): String = "(T $color $left $value $right)"

        override fun <B> foldInReverseOrder(identity: B, f: (B) -> (A) -> (B) -> B): B =
            f(right.foldInReverseOrder(identity, f))(value)(left.foldInReverseOrder(identity, f))
    }

    companion object {
        operator fun <A: Comparable<A>> invoke(): Tree<A> = E

        protected fun <A: Comparable<A>> balance(color: Color, left: Tree<A>, value: A, right: Tree<A>): Tree<A> = when {
            color is Black && left.isTR && left.left.isTR ->
                T(Red,
                    left.left.blacken(), /*T(Black, left.left.left, left.left.value, left.left.right), */
                    left.value,
                    T(Black, left.right, value, right)
                )
            color is Black && left.isTR && left.right.isTR ->
                T(Red,
                    T(Black, left.left, left.value, left.right.left),
                    left.right.value,
                    T(Black, left.right.right, value, right)
                )
            color is Black && right.isTR && right.left.isTR ->
                T(Red,
                    T(Black, left, value, right.left.left),
                    right.left.value,
                    T(Black, right.left.right, right.value, right.right)
                )
            color is Black && right.isTR && right.right.isTR ->
                T(Red,
                    T(Black, left, value, right.left),
                    right.value,
                    right.right.blacken() /* T(Black, right.right.left, right.right.value, right.right.right) */
                )
            else -> T(color, left, value, right)
        }
    }

    protected fun blacken(): Tree<A> = when (this) {
        is Empty -> E
        is T -> T(Black, left, value, right)
    }

    protected fun add(element: @UnsafeVariance A): Tree<A> = when (this) {
        is Empty -> T(Red, E, element, E)
        is T -> when {
            element < this.value -> balance(color, left.add(element), value, right)
            element > this.value -> balance(color, left, value, right.add(element))
            else -> T(color, left, element, right) // replace the same node, no need to rebalance
        }
    }

    operator fun plus(value: @UnsafeVariance A): Tree<A> = this.add(value).blacken()

    // Removal function not implemented
    operator fun minus(value: @UnsafeVariance A): Tree<A> = TODO("minus")

    fun contains(a: @UnsafeVariance A): Boolean = when (this) {
        is Empty -> false
        is T -> when {
            a < this.value -> this.left.contains(a)
            a > this.value -> this.right.contains(a)
            else -> true
        }
    }

    operator fun get(a: @UnsafeVariance A): Result<A> = when (this)  {
        is Empty -> Result()
        is T -> when {
            a < this.value -> this.left.get(a)
            a > this.value -> this.right.get(a)
            else -> Result(this.value)
        }
    }
}

fun main(args: Array<String>) {
    val tree = Tree<Int>() + 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10 + 11
    println(tree)
}