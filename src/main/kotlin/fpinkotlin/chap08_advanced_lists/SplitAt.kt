package fpinkotlin.chap08_advanced_lists

fun <A> MList<A>.splitAtWithFold(index: Int): Pair<MList<A>, MList<A>> {
    val i = if (index < 0) 0 else if (index > length) length else index
    val identity = Triple<MList<A>, MList<A>, Int>(MList(), MList(), i)
    return foldLeft(identity) { ta ->
        { a ->
            if (ta.third == 0)
                Triple(ta.first, ta.second.cons(a), ta.third)
            else
                Triple(ta.first.cons(a), ta.second, ta.third)
        }
    }.let { it.first.reverse() to it.second.reverse() }
}

fun <A, B> MList<A>.foldLeftAbsorbing(identity: B, zero: B, f: (B) -> (A) -> B): Pair<B, MList<A>> {
    fun foldLeftAbsorbing_(acc: B, zero: B, list: MList<A>, f: (B) -> (A) -> B): Pair<B, MList<A>> = when (list) {
        is MList.Nil -> acc to list
        is MList.Cons -> if (acc == zero) acc to list else foldLeftAbsorbing_(f(acc)(list.head), zero, list.tail, f)
    }
    return when (this) {
        is MList.Nil -> identity to MList()
        is MList.Cons -> foldLeftAbsorbing_(identity, zero, this, f)
    }
}

// Ex. 8.15 not implemented due to namespace clashes