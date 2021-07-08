package fpinkotlin.chap09_laziness

import fpinkotlin.chap08_advanced_lists.MList
import fpinkotlin.chap08_advanced_lists.map
import fpinkotlin.chap08_advanced_lists.traverse
import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap07_exceptions.map2

fun <A> sequence(lst: MList<Lazy<A>>): Lazy<MList<A>> =
    Lazy { lst.map { it() } }

fun <A> Result.Companion.of(lazy: Lazy<A>): Result<A> = try {
    Result(lazy.invoke())
} catch (e: Exception) {
    Result.failure(e)
}

//fun <A> sequenceResult(lst: MList<Lazy<A>>): Lazy<Result<MList<A>>> =
//    Lazy { fpinkotlin.chap08_advanced_lists.sequence(lst.map { Result.of(it) }) }
//
//fun <A> sequenceResult2(lst: MList<Lazy<A>>): Lazy<Result<MList<A>>> =
//    Lazy { traverse(lst) { Result.of(it) } }

// Escaping version

// An adapted foldLeft version to receive a predicate callback
fun <A, B> MList<A>.foldLeft(identity: B, pred: (B) -> Boolean, f: (B) -> (A) -> B): B {
    fun foldLeft_(acc: B, list: MList<A>, f: (B) -> (A) -> B): B = when (list) {
        is MList.Nil -> acc
        is MList.Cons -> if (pred(acc)) acc else foldLeft_(f(acc)(list.head), list.tail, f)
    }
    return when (this) {
        is MList.Nil -> identity
        is MList.Cons ->foldLeft_(identity, this, f)
    }
}

// Warning: Due to the nature of foldLeft, while this function is evaluating from the left hand side
// of a list to its right hand side, the constructed sequence in Result is inverted
// This might be resolved by changing the foldLeft function used in implementation
fun <A> sequenceResult(list: MList<Lazy<A>>): Lazy<Result<MList<A>>> = Lazy {
    val p = { r: Result<MList<A>> -> r.map { false }.getOrElse(true) }
    list.foldLeft(Result(MList()), p) { y: Result<MList<A>> ->
        { x: Lazy<A> ->
            map2(Result.of(x), y) { a: A ->
                { b: MList<A> ->
                    b.cons(a)
                }
            }
        }
    }
}
