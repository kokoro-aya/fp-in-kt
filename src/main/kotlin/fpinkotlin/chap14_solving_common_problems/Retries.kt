package fpinkotlin.chap14_solving_common_problems

import fpinkotlin.chap13_actor_framework.common.Result
import java.util.*

fun <A, B> retry(f: (A) -> B, times: Int, delay: Long = 10L): (A) -> Result<B> {
    fun retry(a: A, result: Result<B>, e: Result<B>, tms: Int): Result<B> =
        result.orElse {
            when (tms) {
                0 -> e
                else -> {
                    Thread.sleep(delay)
                    println("Retry ${times - tms}")
                    retry(a, Result.of { f(a) }, result, tms - 1)
                }
            }
        }
    return { a -> retry (a, Result.of { f(a) }, Result(), times - 1)}
}

fun show(message: String) = Random().nextInt(10).let {
    when {
        it < 8 -> throw IllegalStateException("Failure !!!")
        else -> println(message)
    }
}

fun main () {
    retry(::show, 10, 20) ("Hello world!").forEach(onFailure = { println(it.message) })
}