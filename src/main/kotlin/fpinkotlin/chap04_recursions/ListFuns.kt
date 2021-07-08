package fpinkotlin.chap04_recursions

fun <T> head(list: List<T>): T =
    if (list.isEmpty())
        throw IllegalArgumentException("head called on empty list")
    else
        list.first()

fun <T> tail(list: List<T>): List<T> =
    if (list.isEmpty())
        throw IllegalArgumentException("tail called on empty list")
    else
        list.drop(1)

fun sum(list: List<Int>): Int {
    tailrec fun sumTail(list: List<Int>, acc: Int): Int =
        if (list.isEmpty())
            acc
        else
            sumTail(tail(list), acc + head(list))

    return sumTail(list, 0)
}

//fun <T> makeString(list: List<T>, delim: String): String = when {
//    list.isEmpty() -> ""
//    tail(list).isEmpty() -> "${head(list)}${makeString(tail(list), delim)}"
//    else -> "${head(list)}$delim${makeString(tail(list), delim)}"
//}

fun <T> makeString(list: List<T>, delim: String): String {
    tailrec fun makeStringTail(list: List<T>, acc: String): String =
        when {
            acc.isEmpty() -> makeStringTail(tail(list), "${head(list)}")
            list.isEmpty() -> acc
            else -> makeStringTail(tail(list), "$acc$delim${head(list)}")
        }
    return makeStringTail(list, "")
}

fun main() {
    println(makeString(listOf(1,2,3,4,5), " -> "))
}