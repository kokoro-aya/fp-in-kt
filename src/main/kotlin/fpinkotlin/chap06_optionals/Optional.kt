package fpinkotlin.chap06_optionals

import java.util.*

sealed class Optional<out A> {
    abstract fun isEmpty(): Boolean

    internal object None: Optional<Nothing>() {
        override fun isEmpty(): Boolean = true
        override fun toString(): String = "None"
        override fun equals(other: Any?): Boolean = other === None
        override fun hashCode(): Int = 0
    }

    internal data class Some<out A>(internal val value: A): Optional<A>() {
        override fun isEmpty(): Boolean = false
    }

    fun getOrElse(default: () -> @UnsafeVariance A): A = when (this) {
        is None -> default.invoke() // this branch is called lazily to prevent problem of eager computation
        is Some -> this.value
    }

    fun <B> map(f: (A) -> B): Optional<B> = when (this) {
        is None -> None
        is Some -> Some(f(this.value))
    }

    fun <B> flatMap(f: (A) -> Optional<B>): Optional<B> = when (this) {
        is None -> None
        is Some -> f(this.value)
    }

    // fun <B> flatMap(f: (A) -> Option<B>): Option<B> = map(f).getOrElse(None)

    fun orElse(default: () -> Optional<@UnsafeVariance A>): Optional<A> =
        map { _ -> this }.getOrElse(default)

    fun filter(p: (A) -> Boolean): Optional<A> =
        flatMap { x -> if (p(x)) this else None }

    companion object {
        operator fun <A> invoke(a: A? = null): Optional<A> = when (a) {
            null -> None
            else -> Some(a)
        }
    }
}

//fun <A, B> lift(action: (A) -> B): (Optional<A>) -> Optional<B> = { it.map(action) }
// useless with exception functions

fun <A, B> lift(action: (A) -> B): (Optional<A>) -> Optional<B> = {
    try {
        it.map(action)
    } catch (e: Exception) {
        Optional()
    }
}

fun <A, B> hLift(f: (A) -> B): (A) -> Optional<B> = {
    try {
        Optional(it).map(f)
    } catch (e: Exception) {
        Optional()
    }
}

val upperOption: (Optional<String>) -> Optional<String> = lift(String::toUpperCase)

fun max(list: List<Int>): Optional<Int> = Optional(list.maxOrNull())

fun getDefault(): Int = throw RuntimeException()

fun main() {
    val max1 = max(listOf(3,5,7,2,1)).getOrElse(::getDefault)
    println(max1)
    val max2 = max(listOf()).getOrElse(::getDefault)
    println(max2)
}