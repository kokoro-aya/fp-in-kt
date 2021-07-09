package fpinkotlin.chap12_effects

import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap08_advanced_lists.MList
import fpinkotlin.chap09_laziness.Stream

// Ex. 12.2 ReadConsole

data class Person(val id: Int, val firstName: String, val lastName: String)

fun person(input: Input): Result<Pair<Person, Input>> = input.readInt("Enter your ID")
    .flatMap { id ->
        id.second.readString("Enter your first name")
            .flatMap { first ->
                first.second.readString("Enter your last name")
                    .flatMap { last ->
                        Result(Person(id.first, first.first, last.first) to last.second)
                    }
            }
    }

fun readPersonsFromConsole(): MList<Person> = Stream.unfold(ConsoleReader(), ::person).toList()

//fun main() {
//    readPersonsFromConsole().forEach(::println)
//}

// Ex 12.3 ReadFile

fun readPersonsFromFile(path: String): Result<MList<Person>> =
    FileReader(path).map {
        it.use {
            Stream.unfold(it, ::person).toList()
        }
    }

//fun main() {
//    val path = "data.txt"
//    readPersonsFromFile(path).forEachOrElse({ list -> list.forEach(::println)}, onFailure = ::println)
//}


// Using ScriptReader

fun readPersonsFromScript(vararg commands: String): MList<Person> =
    Stream.unfold(ScriptReader(*commands), ::person).toList()

fun main() {
    readPersonsFromScript("1", "Mickey", "Mouse", "2", "Minnie", "Mouse", "3", "Donald", "Duck")
        .forEach(::println)
}