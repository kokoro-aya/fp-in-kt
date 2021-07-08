package fpinkotlin.chap07_exceptions

import java.io.IOException

fun <K, V> Map<K, V>.getResult(key: K): Result<V> = when {
    this.containsKey(key) -> Result(this[key])
    else -> Result.Empty
}

data class ToonR private constructor (
    val firstName: String, val lastName: String, val email: Result<String>) {
    companion object {
        operator fun invoke(firstName: String, lastName: String) =
            ToonR(firstName, lastName, Result.Empty)

        operator fun invoke(firstName: String, lastName: String, email: String) =
            ToonR(firstName, lastName, Result(email))
    }
}

fun getName(): Result<String> = try {
    validate(readLine())
} catch (e: IOException) {
    Result.failure(e)
}

fun validate(name: String?): Result<String> = when {
    name?.isNotEmpty() ?: false -> Result(name)
    else -> Result.Companion.failure("Invalid name $name")
}

fun main() {
    val toons: Map<String, ToonR> = mapOf(
        "Mickey" to ToonR("Mickey", "Mouse", "mickey@disney.com"), "Minnie" to ToonR("Minnie", "Mouse"),
        "Donald" to ToonR("Donald", "Duck", "donald@disney.com"))

    val toon = getName()
        .flatMap(toons::getResult)
        .flatMap(ToonR::email)

    println(toon)


    fun getFirstName(): Result<String> = Result("Mickey")
    fun getLastName(): Result<String> = Result("Mouse")
    fun getMail(): Result<String> = Result("mickey@disney.com")

    val createPerson: (String) -> (String) -> (String) -> ToonR =
        { x -> { y -> { z -> ToonR(x, y, z) } } }

    val newToon = lift3(createPerson)(getFirstName())(getLastName())(getMail())

    val anotherToon = getFirstName()
        .flatMap { firstName ->
            getLastName()
                .flatMap { lastName ->
                    getMail()
                        .map { mail -> ToonR(firstName, lastName, mail) }
                }
        }
}