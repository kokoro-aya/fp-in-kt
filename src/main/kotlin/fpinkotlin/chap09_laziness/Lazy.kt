package fpinkotlin.chap09_laziness

import java.util.*

//fun main(args: Array<String>) {
//    val first = Lazy {
//        println("Evaluating first")
//        true
//    }
//    val second = Lazy {
//        println("Evaluating second")
//        throw IllegalStateException()
//    }
//
//    println(first() || second())
//    println(first() || second())
//    println(or(first, second))
//}

fun or(a: Lazy<Boolean>, b: Lazy<Boolean>): Boolean = if (a()) true else b()

fun constructMessage(greeting: String, name: String): String = "$greeting, $name!"

fun main(args: Array<String>) {
    val greetings = Lazy {
        println("Evaluating greetings")
        "Hello"
    }

    val name1: Lazy<String> = Lazy {
        println("Evaluating name 1")
        "Mickey"
    }

    val name2: Lazy<String> = Lazy {
        println("Evaluating name 2")
        "Donald"
    }

    val defaultMessage = Lazy {
        println("Evaluating default message")
        "No greetings when time is odd"
    }

    val message1 = constructMessage(greetings, name1)
    val message2 = constructMessage(greetings, name2)
    val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0
    println(if (condition) message1() else defaultMessage())
    println(if (condition) message1() else defaultMessage())
    println(if (condition) message2() else defaultMessage())
}

fun constructMessage(greeting: Lazy<String>, name: Lazy<String>): Lazy<String> = Lazy {
    "${greeting()}, ${name()}!"
}

val constructMessageVal: (Lazy<String>) -> (Lazy<String>) -> Lazy<String> = { greeting ->
    { name ->
        Lazy {
            "${greeting()}, ${name()}!"
        }
    }
}

class Lazy<out A>(function: () -> A): () -> A {

    private val value: A by lazy(function)

    override fun invoke(): A = value

    fun <B> map(f: (A) -> B): Lazy<B> = Lazy { f(value) }

    fun <B> flatMap(f: (A) -> Lazy<B>): Lazy<B> = Lazy { f(value)() }


    companion object {
        val lift2Lazy: ((String) -> (String) -> String) -> (Lazy<String>) -> (Lazy<String>) -> Lazy<String> = { f ->
            { x ->
                { y ->
                    Lazy { f(x())(y()) }
                }
            }
        }

    }
}

fun <A, B, C> lift2(f: (A) -> (B) -> C): (Lazy<A>) -> (Lazy<B>) -> Lazy<C> = { x ->
    { y ->
        Lazy { f(x())(y()) }
    }
}