package fpinkotlin.chap08_advanced_lists

import fpinkotlin.chap07_exceptions.Result

fun <A, B, C> zipWith(list1: MList<A>, list2: MList<B>, f: (A) -> (B) -> C): MList<C> {
    tailrec fun zipWith_(acc: MList<C>, list1: MList<A>, list2: MList<B>): MList<C> {
        return when {
            list1 is MList.Nil -> acc
            list2 is MList.Nil -> acc
            else -> zipWith_(acc.cons(f((list1 as MList.Cons).head)((list2 as MList.Cons).head)),
                (list1 as MList.Cons).tail, list2.tail) }
    }
    return zipWith_(MList(), list1, list2).reverse()
}

fun <A, B, C> product(list1: MList<A>, list2: MList<B>, f: (A) -> (B) -> C): MList<C> =
    list1.flatMap { a -> list2.map { b -> f(a)(b) } }.reverse()

//fun main(args: Array<String>) {
//    println(product(MList(1, 2), MList(4, 5, 6)) { x -> { y -> Pair(x, y) } }.toString())
//
//    println(zipWith(MList(1, 2), MList(4, 5, 6)) { x -> { y -> Pair(x, y) } }.toString())
//}

//fun <A, B> unzip(list: MList<Pair<A, B>>): Pair<MList<A>, MList<B>> =
//    list.foldRight(Pair(MList(), MList())) { pair ->
//        { acc ->
//            acc.first.cons(pair.first) to acc.second.cons(pair.second)
//        }
//    }

fun <A, A1, A2> MList<A>.unzip(f: (A) -> Pair<A1, A2>): Pair<MList<A1>, MList<A2>> =
    this.foldRight(Pair(MList(), MList())) { elem ->
        { acc ->
            f(elem).let {
                acc.first.cons(it.first) to acc.second.cons(it.second)
            }
        }
    }

fun <A, B> unzip(list: MList<Pair<A, B>>): Pair<MList<A>, MList<B>> =
    list.unzip { it }

fun <A> MList<A>.get(index: Int): Result<A> {
    tailrec fun <A> getAt_(list: MList<A>, index: Int): Result<A> = when (list) {
        is MList.Nil -> Result.failure("Dead code. Should never execute.")
        is MList.Cons -> if (length == 0) Result(list.head) else getAt_(list.tail, index - 1)
    }

    return if (index < 0 || index >= length) Result.failure("Index out of bound") else getAt_(this, index)
}

fun <A> MList<A>.getAtViaFoldLeft(index: Int): Result<A> =
    (Result.failure<A>("Index out of bound") to index).let {
        if (index < 0 || index >= length) it
        else
            foldLeft(it) { ta ->
                { a ->
                    if (ta.second < 0) ta else Result(a) to (ta.second - 1)
                }
            }
    }.first

// Ex. 8.13 (Solution directly)

fun <A, B> MList<A>.foldLeft(identity: B, zero: B, f: (B) -> (A) -> B): B {
    tailrec fun foldLeft_(acc: B, zero: B, list: MList<A>, f: (B) -> (A) -> B): B = when (list) {
        is MList.Nil -> acc
        is MList.Cons -> if (acc == zero) acc else foldLeft_(f(acc)(list.head), zero, list.tail, f)
    }
    return when (this) {
        is MList.Nil -> identity
        is MList.Cons ->foldLeft_(identity, zero, this, f)
    }
}

fun <A> MList<A>.getAt(index: Int): Result<A> {
    data class SPair<out A>(val first: Result<A>, val second: Int) {
        override fun equals(other: Any?): Boolean = when {
            other == null -> false
            other.javaClass == this.javaClass -> (other as SPair<A>).second == second
            else -> false
        }

        override fun hashCode(): Int = first.hashCode() + second.hashCode()
    }

    return SPair<A>(Result.failure("Index out of bound"), index)
        .let { identity ->
            SPair<A>(Result.failure("Index out of bound"), -1).let { zero ->
                if (index < 0 || index >= length)
                    identity
                else
                    foldLeft(identity, zero) { ta ->
                        { a ->
                            if (ta.second < 0) ta else SPair(Result(a), ta.second - 1)
                        }
                    }
            }
        }.first
}

    // Not implemented: getAt function with foldLeft with predicates

fun <A> MList<A>.splitAt(index: Int): Pair<MList<A>, MList<A>> {
    tailrec fun splitAt_(acc: MList<A>, list: MList<A>, i: Int): Pair<MList<A>, MList<A>> = when (list) {
        is MList.Nil -> list.reverse() to acc
        is MList.Cons ->
            if (i == 0) Pair(list.reverse(), acc) else splitAt_(acc.cons(list.head), list.tail, i - 1)
    }
    return when {
        index < 0 -> splitAt(0)
        index > length -> splitAt(length)
        else -> splitAt_(MList(), this.reverse(), this.length - index)
    }
}