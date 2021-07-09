package fpinkotlin.chap12_effects

import java.io.BufferedReader
import java.io.Closeable
import fpinkotlin.chap07_exceptions.Result
import fpinkotlin.chap08_advanced_lists.MList
import java.io.File
import java.io.InputStreamReader
import kotlin.Exception

interface Input: Closeable {
    fun readString(): Result<Pair<String, Input>>

    fun readInt(): Result<Pair<Int, Input>>

    fun readString(message: String): Result<Pair<String, Input>> = readString()

    fun readInt(message: String): Result<Pair<Int, Input>> = readInt()
}

abstract class AbstractReader(private val reader: BufferedReader): Input {
    override fun readString(): Result<Pair<String, Input>> = try {
        reader.readLine().let {
            when {
                it.isEmpty() -> Result()
                else -> Result(it to this)
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun readInt(): Result<Pair<Int, Input>> = try {
        reader.readLine().let {
            when {
                it.isEmpty() -> Result()
                else -> Result(it.toInt() to this)
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun close() = reader.close()
}

class ConsoleReader(reader: BufferedReader): AbstractReader(reader) {
    override fun readString(message: String): Result<Pair<String, Input>> {
        print("$message ")
        return readString()
    }

    override fun readInt(message: String): Result<Pair<Int, Input>> {
        print("$message ")
        return readInt()
    }

    companion object {
        operator fun invoke(): ConsoleReader =
            ConsoleReader(BufferedReader(InputStreamReader(System.`in`)))
    }
}

class FileReader private constructor(private val reader: BufferedReader): AbstractReader(reader), AutoCloseable {
    override fun close() {
        reader.close()
    }

    companion object {
        operator fun invoke(path: String): Result<Input> = try {
            Result(FileReader(File(path).bufferedReader()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ScriptReader: Input {
    private val commands: MList<String>

    constructor(commands: MList<String>): super() {
        this.commands = commands
    }

    constructor(vararg commands: String): super() {
        this.commands = MList(*commands)
    }

    override fun close() {}

    override fun readString(): Result<Pair<String, Input>> = when {
        commands.isEmpty() -> Result.failure("Not enough entries in script")
        else -> Result(Pair(commands.headSafe().getOrElse(""), ScriptReader(commands.drop(1))))
    }

    override fun readInt(): Result<Pair<Int, Input>> = try {
        when {
            commands.isEmpty() -> Result.failure("Not enough entries in script")
            Integer.parseInt(commands.headSafe().getOrElse("")) >= 0 ->
                Result(Pair(Integer.parseInt(commands.headSafe().getOrElse("")),
                    ScriptReader(commands.drop(1))
                    ))
            else -> Result()
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}

fun main() {
    val input = ConsoleReader()
    val rString = input.readString("Enter your name:")
        .map { it.first }
    val nameMessage = rString.map { "Hello, $it!" }

    nameMessage.forEachOrElse(::println, onFailure = { println(it.message) })

    val rInt = input.readInt("Enter your age:")
        .map { it.first }
    val ageMessage = rInt.map { "You look younger than $it!" }

    ageMessage.forEachOrElse(::println, onFailure = { println("Invalid age. Please enter an integer") })

}