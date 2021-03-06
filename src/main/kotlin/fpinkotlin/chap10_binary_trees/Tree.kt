package fpinkotlin.chap10_binary_trees

import fpinkotlin.chap08_advanced_lists.MList
import fpinkotlin.chap07_exceptions.Result
import kotlin.math.abs
import kotlin.math.max

sealed class Tree<out A: Comparable<@UnsafeVariance A>>{
    abstract fun isEmpty(): Boolean

    abstract val size: Int
    abstract val height: Int

    abstract val max: Result<A>
    abstract val min: Result<A>

    // mystery fold functions
    abstract fun <B> foldLeft(identity: B, f: (B) -> (A) -> B, g: (B) -> (B) -> B): B
    abstract fun <B> foldRight(identity: B, f: (A) -> (B) -> B, g: (B) -> (B) -> B): B

    abstract fun <B> foldInOrder(identity: B, f: (B) -> (A) -> (B) -> B): B
    abstract fun <B> foldPreOrder(identity: B, f: (A) -> (B) -> (B) -> B): B
    abstract fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (A) -> B): B

    protected abstract fun rotateRight(): Tree<A>
    protected abstract fun rotateLeft(): Tree<A>

    abstract fun merge(tree: Tree<@UnsafeVariance A>): Tree<A>

    internal abstract val value: A
    internal abstract val left: Tree<A>
    internal abstract val right: Tree<A>

    internal object Empty: Tree<Nothing>() {
        override fun isEmpty(): Boolean = true
        override fun toString(): String = "E"

        override val size: Int = 0
        override val height: Int = -1

        override val max: Result<Nothing> get() = Result.Empty
        override val min: Result<Nothing> get() = Result.Empty

        override fun merge(tree: Tree<Nothing>): Tree<Nothing> = tree

        override fun <B> foldLeft(identity: B, f: (B) -> (Nothing) -> B, g: (B) -> (B) -> B): B = identity
        override fun <B> foldRight(identity: B, f: (Nothing) -> (B) -> B, g: (B) -> (B) -> B): B = identity

        override fun <B> foldInOrder(identity: B, f: (B) -> (Nothing) -> (B) -> B): B = identity
        override fun <B> foldPreOrder(identity: B, f: (Nothing) -> (B) -> (B) -> B): B = identity
        override fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (Nothing) -> B): B = identity

        override fun rotateRight(): Tree<Nothing> = this

        override fun rotateLeft(): Tree<Nothing> = this

        override val value: Nothing by lazy {
            throw IllegalStateException("No value in Empty")
        }
        override val left: Tree<Nothing> by lazy {
            throw IllegalStateException("No left in Empty")
        }
        override val right: Tree<Nothing> by lazy {
            throw IllegalStateException("No right in Empty")
        }
    }

    internal class T<out A: Comparable<@UnsafeVariance A>> (
        override val left: Tree<A>,
        override val value: A,
        override val right: Tree<A>): Tree<A>() {
        override fun isEmpty(): Boolean = false
        override fun toString(): String = "(T $left $value $right)"

        override val size: Int = 1 + left.size + right.size
        override val height: Int = 1 + max(left.height, right.height)

        override val max: Result<A>
            get() = right.max.orElse { Result(value) }
        override val min: Result<A>
            get() = left.min.orElse { Result(value) }

        override fun merge(tree: Tree<@UnsafeVariance A>): Tree<A> = when (tree) {
            is Empty -> this
            is T -> when {
                tree.value > this.value ->
                    T(left, value, right.merge(T(Empty, tree.value, tree.right)))
                        .merge(tree.left)
                tree.value < this.value ->
                    T(left.merge(T(tree.left, tree.value, Empty)), value, right)
                        .merge(tree.right)
                else ->
                    T(left.merge(tree.left), value, right.merge(tree.right))
            }
        }

        override fun <B> foldLeft(identity: B, f: (B) -> (A) -> B, g: (B) -> (B) -> B): B {
            val fr = right.foldLeft(identity, f, g)
            val fl = f(left.foldLeft(identity, f, g))
            return g(fr)((fl)(this.value))
        }

        override fun <B> foldRight(identity: B, f: (A) -> (B) -> B, g: (B) -> (B) -> B): B {
            val fr = right.foldRight(identity, f, g)
            val fl = left.foldRight(identity, f, g)
            return g(f(this.value)(fl))(fr)
        }

        override fun <B> foldPreOrder(identity: B, f: (A) -> (B) -> (B) -> B): B =
            f(value)(left.foldPreOrder(identity, f))(right.foldPreOrder(identity, f))

        override fun <B> foldInOrder(identity: B, f: (B) -> (A) -> (B) -> B): B =
            f(left.foldInOrder(identity, f))(value)(right.foldInOrder(identity, f))

        override fun <B> foldPostOrder(identity: B, f: (B) -> (B) -> (A) -> B): B =
            f(right.foldPostOrder(identity, f))(left.foldPostOrder(identity, f))(value)

        override fun rotateRight(): Tree<A> = when (left) {
            is Empty -> this
            is T -> T(left.left, left.value,
                T(left.right, value, right))
        }

        override fun rotateLeft(): Tree<A> = when (right) {
            is Empty -> this
            is T -> T(T(left, value, right.left), right.value, right.right)
        }
    }

    operator fun plus(element: @UnsafeVariance A): Tree<A> = when (this) {
        is Empty -> T(Empty, element, Empty)
        is T -> when {
            element < this.value -> T(this.left + element, this.value, this.right)
            element > this.value -> T(this.left, this.value, this.right + element)
            else -> T(this.left, element, this.right)
        }
    }

    fun contains(a: @UnsafeVariance A): Boolean = when (this) {
        is Empty -> false
        is T -> when {
            a < this.value -> this.left.contains(a)
            a > this.value -> this.right.contains(a)
            else -> true
        }
    }

    // this and ta cannot have the same root
    protected fun removeMerge(ta: Tree<@UnsafeVariance A>): Tree<A> = when (this) {
        is Empty -> ta
        is T -> when (ta) {
            is Empty -> this // this can't be empty
            is T -> when {
                ta.value < value -> T(left.removeMerge(ta), value, right)
                else -> T(left, value, right.removeMerge(ta))

            }
        }
    }

    fun remove(a: @UnsafeVariance A): Tree<A> = when (this) {
        Empty -> this
        is T -> when {
            a < value -> T(left.remove(a), value, right)
            a > value -> T(left, value, right.remove(a))
            else -> left.removeMerge(right)
        }
    }

    fun <B: Comparable<B>> map(f: (A) -> B): Tree<B> =
        foldInOrder(Empty) { x: Tree<B> ->
            { y: A ->
                { z: Tree<B> ->
                    Tree(x, f(y), z)
                }
            }
        }

//    fun toListInOrderRight(): MList<A> = when (this) {
//        is Empty -> MList()
//        is T -> right.toListInOrderRight()
//            .concat(MList(value))
//            .concat(left.toListInOrderRight())
//    } // Will stack overflow

    fun toListInOrderRight(): MList<A> = unBalanceRight(MList(), this)


    fun <A: Comparable<A>> isUnBalanced(): Boolean = when (this) {
        Empty -> false
        is T -> Math.abs(left.height - right.height) > (this.size - 1) % 2
    }

    companion object {
        operator fun <A: Comparable<A>> invoke(): Tree<A> = Empty

        operator fun <A: Comparable<A>> invoke(vararg az: A): Tree<A> =
            az.foldRight(Empty) { a: A, acc: Tree<A> -> acc + a }

        operator fun <A: Comparable<A>> invoke(il: MList<A>): Tree<A> =
            il.foldLeft(Empty) { acc: Tree<A> -> { a: A -> acc + a } }

        operator fun <A: Comparable<A>> invoke(left: Tree<A>, a: A, right: Tree<A>): Tree<A> = when {
            ordered(left, a, right) -> T(left, a, right)
            ordered(right, a, left) -> T(right, a, left)
            else -> Tree(a).merge(left).merge(right)
        }

        private tailrec fun <A: Comparable<A>> unBalanceRight(acc: MList<A>, tree: Tree<A>): MList<A> =
            when (tree) {
                Empty -> acc
                is T -> when (tree.left) {
                    Empty -> unBalanceRight(acc.cons(tree.value), tree.right)
                    is T -> unBalanceRight(acc, tree.rotateRight())
                }
            }

        fun <A> unfold(a: A, f: (A) -> Result<A>): A {
            tailrec fun <A> unfold_(a: Pair<Result<A>, Result<A>>, f: (A) -> Result<A>): Pair<Result<A>, Result<A>> {
                return when (val x = a.second.flatMap { f(it) }) {
                    is Result.Success -> unfold_(a.second to x, f)
                    else -> a
                }
            }
            return Result(a).let { unfold_(it to it, f).second.getOrElse(a) }
        }

        private fun <A: Comparable<A>> balanceFirstLevel(tree: Tree<A>): Tree<A> =
            unfold(tree) { tr: Tree<A> ->
                when {
                    tr.isUnBalanced<A>() -> when {
                        tree.right.height > tree.left.height -> Result(tr.rotateLeft())
                        else -> Result(tr.rotateRight())
                    }
                    else -> Result()
                }
            }

        private fun <A: Comparable<A>> balanceHelper(tree: Tree<A>): Tree<A> = when {
            !tree.isEmpty() && tree.height > log2nlz(tree.size) -> when {
                abs(tree.left.height - tree.right.height) > 1 -> balanceHelper(balanceFirstLevel(tree))
                else -> T(balanceHelper(tree.left), tree.value, balanceFirstLevel(tree.right))
            }
            else -> tree
        }

        fun <A: Comparable<A>> balance(tree: Tree<A>): Tree<A> =
            balanceHelper(tree.toListInOrderRight().foldLeft(Empty) { t: Tree<A> ->
                { a: A ->
                    T(Empty, a, t)
                }
            })
    }
}

// Taken from com.fpinkotlin.common.Result
fun <T> Result<T>.mapEmpty() = when (this) {
    is Result.Empty -> Result(Any())
    is Result.Failure -> Result.failure(this.exception)
    is Result.Success -> Result.failure("Not empty")
}

fun <A: Comparable<A>> lt(first: A, second: A): Boolean = first < second
fun <A: Comparable<A>> lt(first: A, second: A, third: A): Boolean =
    first < second && second < third

fun <A: Comparable<A>> ordered(left: Tree<A>, a: A, right: Tree<A>): Boolean =
    (left.max.flatMap { lMax ->
        right.min.map { rMin ->
            lt(lMax, a, rMin)
        }
    }.getOrElse(left.isEmpty() && right.isEmpty())
            || left.min.mapEmpty().flatMap {
                right.min.map { rMin ->
                    lt(a, rMin)
                }
            }.getOrElse(false)
            || right.min.mapEmpty().flatMap {
                left.max.map { lMax ->
                    lt(lMax, a)
                }
            }.getOrElse(false)) // Just copied from the book

fun log2nlz(n: Int): Int = when (n) {
    0 -> 0
    else -> 31 - Integer.numberOfLeadingZeros(n)
}

fun main() {
    val tree = Tree<Int>() + 3 + 5 + 6 + 9 + 2 + 7 + 1 + 4 + 11
    println(Tree.balance(tree).toString())
}