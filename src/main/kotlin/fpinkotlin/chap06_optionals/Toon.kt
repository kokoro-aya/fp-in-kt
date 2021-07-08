package fpinkotlin.chap06_optionals

data class Toon(val firstName: String, val lastName: String,
                val email: Optional<String> = Optional()
                ) {
    companion object {
        operator fun invoke(firstName: String, lastName: String,
                            email: String? = null
                            ) = Toon(firstName, lastName, Optional(email))
    }
}

fun <K, V> Map<K, V>.getOptional(key: K) = Optional(this[key])

fun main() {
    val toons: Map<String, Toon> = mapOf(
        "Mickey" to Toon("Mickey", "Mouse", "mickey@disney.com"), "Minnie" to Toon("Minnie", "Mouse"),
        "Donald" to Toon("Donald", "Duck", "donald@disney.com"))

    val mickey =
        toons.getOptional("Mickey").flatMap { it.email }
    val minnie = toons.getOptional("Minnie").flatMap { it.email }
    val goofy = toons.getOptional("Goofy").flatMap { it.email }
    println(mickey.getOrElse { "No data" })
    println(minnie.getOrElse { "No data" })
    println(goofy.getOrElse { "No data" })
}