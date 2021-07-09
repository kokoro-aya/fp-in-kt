package fpinkotlin.chap12_effects

import fpinkotlin.chap07_exceptions.Result

fun main() {
    val ra = Result(4)
    val rb = Result(0)
    val inverse: (Int) -> Result<Double> = { x ->
        when {
            x != 0 -> Result(1.toDouble() / x)
            else -> Result.failure("Division by 0")
        }
    }
    val showResult: (Double) -> Unit = ::println
    val showError: (Exception) -> Unit = { println("Error - ${it.message}") }

    val rt1 = ra.flatMap(inverse)
    val rt2 = rb.flatMap(inverse)

    println("Inverse of 4:")
    rt1.forEachOrElse(showResult, showError)

    println("Inverse of 0:")
    rt2.forEachOrElse(showResult, showError)
}