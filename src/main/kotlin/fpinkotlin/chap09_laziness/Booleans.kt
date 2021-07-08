package fpinkotlin.chap09_laziness

//fun main(args: Array<String>) {
//    println(or(true, true))
//    println(or(true, false))
//    println(or(false, true))
//    println(or(false, false))
//    println(and(true, true))
//    println(and(true, false))
//    println(and(false, true))
//    println(and(false, false))
//}
fun or(a: Boolean, b: Boolean): Boolean = if (a) true else b
fun and(a: Boolean, b: Boolean): Boolean = if (a) b else false


//fun main(args: Array<String>) {
//    println(getFirst() || getSecond())
//    println(or(getFirst(), getSecond())) // Exception in thread "main" java.lang.IllegalStateException
//}
//fun getFirst(): Boolean = true
//fun getSecond(): Boolean = throw IllegalStateException()

//fun main(args: Array<String>) {
//    val first: Boolean by lazy { true }
//    val second: Boolean by lazy { throw IllegalStateException() }
//
//    println(first || second)
//    println(or(first, second)) // Exception in thread "main" java.lang.IllegalStateException
//}

// This works
//fun main(args: Array<String>) {
//    val first = { true }
//    val second = { throw IllegalStateException() }
//    println(first() || second())
//    println(or(first, second))
//}

// This also works
fun main(args: Array<String>) {
    fun first() = true
    fun second(): Boolean = throw IllegalStateException()
    println(first() || second())
    println(or(::first, ::second))
}

fun or(a: () -> Boolean, b: () -> Boolean): Boolean = if (a()) true else b()