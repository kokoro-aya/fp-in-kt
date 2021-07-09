package fpinkotlin.chap12_effects

import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap08_advanced_lists.MList
import fpinkotlin.chap09_laziness.Stream
import fpinkotlin.chap09_laziness.Lazy
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class IO<out A>(private val f: () -> A) {
    operator fun invoke() = f()

    operator fun plus(io: IO<@UnsafeVariance A>): IO<A> = IO {
        f()
        io.f()
    }

    companion object {
        val empty: IO<Unit> = IO {}

        operator fun <A> invoke(a: A): IO<A> = IO { a }

        fun <A, B, C> map2(ioa: IO<A>, iob: IO<B>, f: (A) -> (B) -> C): IO<C> = ioa.flatMap { a ->
            iob.map { b ->
                f(a)(b)
            }
        }

        fun <A> repeat(n: Int, io: IO<A>): IO<MList<A>> =
            Stream.fill(n, Lazy { io })
                .foldRight( Lazy { IO { MList<A>() } }) { ioa: IO<A> ->
                    { sio: Lazy<IO<MList<A>>> ->
                        map2(ioa, sio()) { a ->
                            { la -> la.cons(a) }
                        }
                    }
                }

        fun <A, B> forever(ioa: IO<A>): IO<B> {
            val t: () -> IO<B> = { forever(ioa) }
            return ioa.flatMap { t() }
        }
    }

    fun <B> map(g: (A) -> B): IO<B> = IO {
        g(this())
    }

    fun <B> flatMap(g: (A) -> IO<B>): IO<B> = IO {
        g(this())()
    }


}

fun show(message: String): IO<Unit> = IO { println(message) }

fun <A> toString(rd: Result<A>): String =
    rd.map { it.toString() }.getOrElse(rd.toString())

fun inverse(i: Int): Result<Double> = when (i) {
    0 -> Result.failure("Div by 0")
    else -> Result(1.0 / i)
}

//fun main() {
//    fun getName() = "Mickey"
//
//    val inst1 = IO { print("Hello, ") }
//    val inst2 = IO { print(getName()) }
//    val inst3 = IO { print("!\n") }
//
//    val script: IO<Unit> = inst1 + inst2 + inst3
//
//    script()
//
//    val sl = MList(inst1, inst2, inst3)
//    sl.foldRight(IO.empty) { io -> { io + it } }()
//}

object Console {
    private val br = BufferedReader(InputStreamReader(System.`in`))

    fun readln(): IO<String> = IO {
        try {
            br.readLine()
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }
    fun println(o: Any): IO<Unit> = IO { kotlin.io.println(o.toString()) }
    fun print(o: Any): IO<Unit> = IO { kotlin.io.print(o.toString()) }
}

//fun main() {
//    val script = sayHello2()
//    script()
//}

private fun sayHello1(): IO<Unit> = Console.print("Enter your name: ")
    .map { Console.readln()() }
    .map { buildMessage(it) }
    .map { Console.println(it)() }

private fun sayHello2(): IO<Unit> = Console.print("Enter your name: ")
    .flatMap { Console.readln() }
    .map { buildMessage(it) }
    .flatMap{ Console.println(it) }

private fun buildMessage(name: String): String = "Hello, $name!"


//fun main() {
//    val program = IO.repeat(3, sayHello2())
//    program()
//}

// This will blow the stack
//fun main () {
//    val program = IO.forever<String, String>(IO {"Hi again!"})
//        .flatMap { Console.println(it) }
//    program()
//}