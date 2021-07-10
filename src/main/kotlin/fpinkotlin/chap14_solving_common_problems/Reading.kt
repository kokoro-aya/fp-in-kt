package fpinkotlin.chap14_solving_common_problems

import fpinkotlin.chap13_actor_framework.common.List
import fpinkotlin.chap13_actor_framework.common.List.Companion.fromSeparated

import fpinkotlin.chap13_actor_framework.common.Result
import java.io.IOException
import java.io.StringReader
import java.lang.NumberFormatException
import java.lang.invoke.MethodHandles
import java.util.*

class PropertyReader(private val properties: Result<Properties>, private val source: String) {


    fun readAsString(name: String) =
        properties.flatMap { Result.of { it.getProperty(name) }.mapFailure("Property \"$name\" not found") }

    fun readAsInt(name: String): Result<Int> =
        readAsString(name).flatMap {
            try {
                Result(it.toInt())
            } catch (e: NumberFormatException) {
                Result.failure<Int>("Invalid value while parsing property '$name' to Int: '$it'")
            }
        }

    fun <T> readAsList(name: String, f: (String) -> T): Result<List<T>> =
        readAsString(name).flatMap {
            try {
                Result(fromSeparated(it, ",").map(f))
            } catch (e: Exception) {
                Result.failure<List<T>>(
                    "Invalid value while parsing property '$name' to List<T>: '$it'")
            }
        }

    fun readAsIntList(name: String): Result<List<Int>> = readAsList(name, String::toInt)
    fun readAsDoubleList(name: String): Result<List<Double>> = readAsList(name, String::toDouble)
    fun readAsBooleanList(name: String): Result<List<Boolean>> = readAsList(name, String::toBoolean)

    fun <T> readAsType(f: (String) -> Result<T>, name: String) =
        readAsString(name).flatMap {
            try {
                f(it)
            } catch (e: Exception) {
                Result.failure<T>("Invalid value while parsing property '$name': '$it'")
            }
        }

    inline fun <reified T: Enum<T>> readAsEnum(name: String, enumClass: Class<T>): Result<T> {
        val f: (String) -> Result<T> = {
            try {
                val value = enumValueOf<T>(it)
                Result(value)
            } catch (e: Exception) {
                Result.failure("Error parsing property '$name': " +
                        "value '$it' can't be parsed to ${enumClass.name}")
            }
        }
        return readAsType(f, name)
    }

    fun readAsPropertyString(propertyName: String): Result<String> =
        readAsString(propertyName).map { toPropertyString(it) }

    companion object {
        fun toPropertyString(s: String): String = s.replace(";", "\n")

        private fun readPropertiesFromFile(configFileName: String): Result<Properties> =
            try {
                MethodHandles.lookup().lookupClass()
                    .getResourceAsStream(configFileName)
                    .use { inputStream ->
                        when (inputStream) {
                            null -> Result.failure("File $configFileName not found in classpath")
                            else -> Properties().let {
                                it.load(inputStream)
                                Result(it)
                            }
                        }
                    }
            } catch (e: IOException) {
                Result.failure("IOException reading classpath resource $configFileName")
            } catch (e: Exception) {
                Result.failure(
                    "Exception: ${e.message} reading classpath" +
                            " resource $configFileName"
                )
            }

        private fun readPropertiesFromString(propString: String): Result<Properties> =
            try {
                StringReader(propString).use { reader ->
                    val properties = Properties()
                    properties.load(reader)
                    Result(properties)
                }
            } catch (e: Exception) {
                Result.failure(
                    "Exception reading properties string " +
                            "$propString: ${e.message}"
                )
            }

        fun filePropertyReader(fileName: String): PropertyReader =
            PropertyReader(readPropertiesFromFile(fileName), "File: $fileName")

        fun stringPropertyReader(propString: String): PropertyReader =
            PropertyReader(readPropertiesFromString(propString), "String: $propString")
    }
}

//fun main() {
//    val propertyReader = PropertyReader("config.txt")
////    propertyReader.properties.forEach(onSuccess = { println(it) }, onFailure = { println(it) })
//    propertyReader.readProperty("host")
//        .forEach(onSuccess = { println(it) }, onFailure = { println(it) })
//    propertyReader.readProperty("name")
//        .forEach(onSuccess = { println(it) }, onFailure = { println(it) })
//    propertyReader.readProperty("year")
//        .forEach(onSuccess = { println(it) }, onFailure = { println(it) })
//}

fun main() {
    val propertyReader = PropertyReader.stringPropertyReader("/config.properties")
    val person = propertyReader.readAsInt("id")
        .flatMap { id ->
            propertyReader.readAsString("firstName")
                .flatMap { firstName ->
                    propertyReader.readAsString("lastName")
                        .map { lastName -> Person.of(id, firstName, lastName) }
                }
        }

    person.forEach(onSuccess = { println(it) }, onFailure = { println(it) })
}