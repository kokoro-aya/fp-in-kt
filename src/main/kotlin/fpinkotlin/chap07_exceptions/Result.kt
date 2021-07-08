package fpinkotlin.chap07_exceptions

import fpinkotlin.chap06_optionals.Optional
import java.io.Serializable

sealed class Result<out A>: Serializable {

    abstract fun isEmpty(): Boolean

    abstract fun <B> map(f: (A) -> B): Result<B>
    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>

    abstract fun toOptional(): Optional<A>

    abstract fun mapFailure(message: String): Result<A>

//    abstract fun forEach(effect: (A) -> Unit)
    abstract fun forEachOrElse(onSuccess: (A) -> Unit = {},
                               onFailure: (Exception) -> Unit = {},
                               onEmpty: () -> Unit = {})

    internal object Empty: Result<Nothing>() {

        override fun isEmpty(): Boolean = true

        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty
        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty
        override fun toString(): String = "Empty"
        override fun toOptional(): Optional<Nothing> = Optional()

        override fun mapFailure(message: String): Result<Nothing> = this

//        override fun forEach(effect: (Nothing) -> Unit) {}
        override fun forEachOrElse(
            onSuccess: (Nothing) -> Unit,
            onFailure: (Exception) -> Unit,
            onEmpty: () -> Unit
        ) { onEmpty() }
    }

    internal class Failure<out A>(
        internal val exception: Exception
    ): Result<A>() {

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "Failure(${exception.javaClass}:${exception.message})"

        override fun <B> map(f: (A) -> B): Result<B> = Failure(exception)

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = Failure(exception)

        override fun toOptional(): Optional<A> = Optional()

        override fun mapFailure(message: String): Result<A> = failure(RuntimeException(message, exception))

//        override fun forEach(effect: (A) -> Unit) {}

        override fun forEachOrElse(onSuccess: (A) -> Unit, onFailure: (Exception) -> Unit, onEmpty: () -> Unit) {
            onFailure(exception)
        }
    }

    internal class Success<out A>(internal val value: A): Result<A>() {

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "Success($value)"

        override fun <B> map(f: (A) -> B): Result<B> = try {
            Success(f(this.value))
        } catch (e: Exception) {
            Failure(e)
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = try {
            f(this.value)
        } catch (e: Exception) {
            Failure(e)
        }

        override fun toOptional(): Optional<A> = Optional(value)

        override fun mapFailure(message: String): Result<A> = this

//        override fun forEach(effect: (A) -> Unit) { effect(value) }

        override fun forEachOrElse(onSuccess: (A) -> Unit, onFailure: (Exception) -> Unit, onEmpty: () -> Unit) {
            onSuccess(value)
        }
    }

    fun getOrElse(defaultValue: @UnsafeVariance A): A = when (this) {
        is Success -> this.value
        else -> defaultValue
    }
    fun orElse(defaultValue: () -> Result<@UnsafeVariance A>): Result<A> = when (this) {
        is Success -> this
        else -> try {
            defaultValue()
        } catch (e: Exception) {
            Failure(e)
        }
    }

    fun filter(p: (A) -> Boolean): Result<A> = when (this) {
        is Success -> if (p(this.value)) this else failure("Condition not matched")
        else -> this
    }

    fun exists(p: (A) -> Boolean): Boolean = map(p).getOrElse(false)

    companion object {
        operator fun <A> invoke(a: A? = null): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> Success(a)
        }

        operator fun <A> invoke(a: A? = null, message: String): Result<A> = when (a) {
            null -> Failure(NullPointerException(message))
            else -> Success(a)
        }

        operator fun <A> invoke(a: A? = null, p: (A) -> Boolean): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> when (p(a)) {
                true -> Success(a)
                false -> Empty
            }
        }

        operator fun <A> invoke(a: A? = null, message: String,
                                p: (A) -> Boolean): Result<A> = when (a) {
            null -> Failure(NullPointerException())
            else -> when {
                p(a) -> Success(a)
                else -> Failure(IllegalArgumentException(
                    "Argument $a does not match condition: $message"))
            }
        }

        fun <A> failure(message: String): Result<A> =
            Failure(IllegalStateException(message))

        /**
         * A generic failure, warning:: this approach might lost stacktrace
         */
        fun <A> failure(exception: Exception): Result<A> {
            val ctor = exception::class.constructors.find { it.parameters.size == 1
                    && it.parameters.any { it.type.classifier == String::class } }
                ?: throw RuntimeException("Internal state error while initializing Failure with an exception" +
                        "This shouldn't happen.")
            return Failure(ctor.call(exception.message))
        }
    }
}

//fun main() {
//    val a = RuntimeException("Foo")
//    val b = NullPointerException("Bar")
//    val c = ArithmeticException("Foobar")
//    val d = ArrayIndexOutOfBoundsException("Out Of Bounds")
//    val e = RuntimeException(d)
//
//    val ex = listOf(a, b, c, d, e)
//    ex.map { it ->
//        val ctor = it::class.constructors.find { it.parameters.size == 1
//                && it.parameters.any { it.type.classifier == String::class } }
//            ?: throw RuntimeException("Internal state error while initializing Failure with an exception" +
//                    "This shouldn't happen.")
//        ctor.call(it.message)
//    }.forEach { println(it) }
//}

fun <A, B> lift(f: (A) -> B): (Result<A>) -> Result<B> = { it.map(f) }

fun <A, B, C> lift2(f: (A) -> (B) -> C): (Result<A>) -> (Result<B>) -> Result<C> = { a ->
    { b ->
        a.map(f).flatMap { b.map(it) }
    }
}

fun <A, B, C, D> lift3(f: (A) -> (B) -> (C) -> D): (Result<A>) -> (Result<B>) -> (Result<C>) -> Result<D> = { a ->
    { b ->
        { c ->
            a.map(f).flatMap { b.map(it) }.flatMap { c.map(it) }
        }
    }
}

fun <A, B, C> map2(a: Result<A>, b: Result<B>, func: (A) -> (B) -> C): Result<C> = lift2(func)(a)(b)

fun main() {
    val z: Int = 33
    val result: Result<Int> = if (z % 2 == 0) Result(z) else Result.Empty

    result.forEachOrElse({ println("$it is even") }, onEmpty = { println("This one is odd") })
}