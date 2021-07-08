package fpinkotlin.chap07_exceptions

import fpinkotlin.chap05_lists.IList

sealed class Either<out E, out A> {

    abstract fun <B> map(f: (A) -> B): Either<E, B>
    abstract fun <B> flatMap(f: (A) -> Either<@UnsafeVariance E, B>): Either<E, B>

    internal class Left<out E, out A>(internal val value: E): Either<E, A>() {
        override fun toString(): String = "Left($value)"
        override fun <B> map(f: (A) -> B): Either<E, B> = Left(this.value)
        override fun <B> flatMap(f: (A) -> Either<@UnsafeVariance E, B>): Either<E, B> =
            Left(this.value)
    }

    internal class Right<out E, out A>(internal val value: A): Either<E, A>() {
        override fun toString(): String = "Right($value)"
        override fun <B> map(f: (A) -> B): Either<E, B> = Right(f(this.value))
        override fun <B> flatMap(f: (A) -> Either<@UnsafeVariance E, B>): Either<E, B> =
            f(this.value)
    }

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A = when (this) {
        is Left -> defaultValue()
        is Right -> this.value
    }

    fun orElse(defaultValue: () -> Either<@UnsafeVariance E, @UnsafeVariance A>): Either<E, A> = when (this) {
        is Left -> defaultValue()
        is Right -> flatMap { this }.orElse(defaultValue)
    }

    companion object {
        fun <A, B> left(value: A): Either<A, B> = Left(value)
        fun <A, B> right(value: B): Either<A, B> = Right(value)
    }
}

fun <A: Comparable<A>> max(list: IList<A>): Either<String, A> = when (list) {
    is IList.Nil -> Either.left("max called on an empty list")
    is IList.Cons -> Either.right(list.foldLeft(list.head) { x -> { y ->
        if (x >= y) x else y
    }})
}