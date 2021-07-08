package fpinkotlin.chap04_recursions

tailrec fun <T, U> foldLeft(list: List<T>, acc: U, action: (U, T) -> U): U = when {
    list.isEmpty() -> acc
    else -> foldLeft(list.drop(1), action(acc, list.first()), action)
}

fun <T, U> foldRight(list: List<T>, id: U, action: (T, U) -> U): U = when {
    list.isEmpty() -> id
    else -> action(list.first(), foldRight(list.drop(1), id, action))
}

fun foldedSum(list: List<Int>): Int = foldLeft(list, 0, ::add)
fun foldedString(list: List<Char>): String = foldLeft(list, "", String::plus)
fun <T> foldedMakeString(list: List<T>, delim: String): String =
    foldLeft(list, "") { e, f -> if (e.isEmpty()) "$f" else "$e$delim$f" }

//fun main(args: Array<String>) {
//    println(foldedMakeString(listOf(1,2,3,4,5), " -> "))
//}

//fun string(list: List<Char>): String =
//    if (list.isEmpty())
//        ""
//    else
//        prepend(head(list), string(tail(list)))

fun <T> prepend(list: List<T>, elem: T) = listOf(elem) + list

fun <T> reverse(list: List<T>): List<T> {
    return foldLeft(list, listOf(), ::prepend)
}

fun foldedConcat(list: List<Char>): String = foldRight(list, "") { e, f -> "$e$f" }

//fun range(start: Int, end: Int): List<Int> {
//    val res = mutableListOf<Int>()
//    var index = start
//    while (index < end) {
//        res.add(index++)
//    }
//    return res
//}

//fun <T> unfold(seed: T, f:(T) -> T, p: (T) -> Boolean): List<T> {
//    val res = mutableListOf<T>()
//    var elem = seed
//    while (p(elem)) {
//        res.add(elem)
//        elem = f(elem)
//    }
//    return res
//}

//fun range(start: Int, end: Int): List<Int> =
//    unfold(start, {it + 1}, {it < end})

fun range(start: Int, end: Int): List<Int> =
    if (end <= start)
        listOf<Int>()
    else
        prepend(range(start + 1, end), start)

fun <T> unfold(seed: T, f: (T) -> T, p: (T) -> Boolean): List<T> {
    tailrec fun unfold_(acc: List<T>, seed: T,): List<T> =
        if (p(seed))
            unfold_(acc + seed, f(seed))
        else
            acc
    return unfold_(listOf(), seed)
}