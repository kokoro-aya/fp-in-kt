package fpinkotlin.chap04_recursions

fun append(s: String, c: Char): String = "$s$c"

fun toString(list: List<Char>): String {
    tailrec fun toString(list: List<Char>, s: String): String =
        if (list.isEmpty())
            s
        else
            toString(list.drop(1), append(s, list.first()))
    return toString(list, "")
}

fun prepend(c: Char, s: String): String = "$c$s"

fun sum(n: Int): Int {
    tailrec fun sum(s: Int, i: Int): Int =
        if (i > n) s else sum(s + i, i + 1)
    return sum(0, 0)
}

tailrec fun add(a: Int, b: Int): Int =
    if (b == 0) a else add(a.inc(), b.dec())

object Factorial {
    val factorial: (Int) -> Int by lazy {
        { x ->
            if (x <= 1) x else x * factorial(x - 1)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(factorial(10))
    }
}
