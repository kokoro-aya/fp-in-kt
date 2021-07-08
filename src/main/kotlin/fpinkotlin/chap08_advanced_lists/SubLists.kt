package fpinkotlin.chap08_advanced_lists

fun <A> MList<A>.startsWith(sub: MList<@UnsafeVariance A>): Boolean {
    tailrec fun startsWith_(list: MList<A>, sub: MList<A>): Boolean = when (sub) {
        is MList.Nil -> true
        is MList.Cons -> when (list) {
            is MList.Nil -> false
            is MList.Cons -> if (list.head == sub.head) startsWith_(list.tail, sub.tail) else false
        }
    }
    return startsWith_(this, sub)
}

fun <A> MList<A>.hasSubList(sub: MList<@UnsafeVariance A>): Boolean {
    tailrec fun <A> hasSubList_(list: MList<A>, sub: MList<A>): Boolean = when (list) {
        is MList.Nil -> sub.isEmpty()
        is MList.Cons -> if (list.startsWith(sub)) true else hasSubList_(list.tail, sub)
    }
    return hasSubList_(this, sub)
}