package fpinkotlin.chap08_advanced_lists

import fpinkotlin.chap06_optionals.Optional
import fpinkotlin.chap07_exceptions.Result

fun <A, B> MList<A>.groupBy(f: (A) -> B): Map<B, MList<A>> =
    this.reverse().foldLeft(mapOf()) { map ->
        { elem ->
            f(elem).let {
                map + (it to (map[it] ?: MList()).cons(elem))
            }
        }
    }

fun <A, S> unfold(z: S, getNext: (S) -> Optional<Pair<A, S>>): MList<A> {
    tailrec fun unfold_(acc: MList<A>, z: S): MList<A> {
        val next = getNext(z)
        return when (next) {
            is Optional.None -> acc
            is Optional.Some -> unfold_(acc.cons(next.value.first), next.value.second)
        }
    }
    return unfold_(MList(), z).reverse()
}

fun <A, S> unfoldWithResult(z: S, getNext: (S) -> Result<Pair<A, S>>): Result<MList<A>> {
    tailrec fun unfold_(acc: MList<A>, z: S): Result<MList<A>> {
        val next = getNext(z)
        return when (next) {
            Result.Empty -> Result(acc)
            is Result.Failure -> Result.Failure(next.exception)
            is Result.Success -> unfold_(acc.cons(next.value.first), next.value.second)
        }
    }
    return unfold_(MList(), z).map(MList<A>::reverse)
}

fun range(start: Int, end: Int): MList<Int> = unfold(start) { i ->
    if (i < end)
        Optional(Pair(i, i + 1))
    else
        Optional()
}

//fun <A> MList<A>.exists(p: (A) -> Boolean): Boolean = when (this) {
//    is MList.Nil -> false
//    is MList.Cons -> p(head) || tail.exists(p)
//}

fun <A> MList<A>.exists(p: (A) -> Boolean): Boolean =
    foldLeft(identity = false, zero = true) { hold ->
        { next ->
            hold || p(next)
        }
    }

fun <A> MList<A>.forAll(f: (A) -> Boolean): Boolean =
    foldLeft(identity = true, zero = false) { hold ->
        { next ->
            hold && f(next)
        }
    }

//fun <A> MList<A>.forAll(f: (A) -> Boolean): Boolean = !exists { !f(it) }

fun main() {
    val f: (Int) -> Optional<Pair<Int, Int>> = { it ->
        if (it < 1_000) Optional(Pair(it, it + 1)) else Optional()
    }
    val result = unfold(0, f)
    println(result)
}