package fpinkotlin.chap14_solving_common_problems

import fpinkotlin.chap13_actor_framework.common.List
import fpinkotlin.chap13_actor_framework.common.Result
import fpinkotlin.chap13_actor_framework.common.sequence
import fpinkotlin.chap14_solving_common_problems.PropertyReader.Companion.stringPropertyReader

fun inverse(x: Int): Result<Double> = when (x) {
    0 -> Result.failure("div. by 0")
    else -> Result(1.0 / x)
}

class Person private constructor(val id: Int, val firstName: String, val lastName: String) {
    companion object {
        fun of(id: Int?, firstName: String?, lastName: String?) =
            Result.of(::isPositive, id, "Negative id")
                .flatMap { validId ->
                    Result.of(::isValidName, firstName, "Invalid first name")
                        .flatMap { validFirstName ->
                            Result.of(::isValidName, lastName, "Invalid last name")
                                .map { validLastName ->
                                    Person(validId!!, validFirstName!!, validLastName!!)
                                }
                        }
                }
        fun isPositive(i: Int?): Boolean = i != null && i > 0
        fun isValidName(name: String?): Boolean =
            name != null && name[0].code in 65 .. 91

        fun readAsPerson(propertyName: String, propertyReader: PropertyReader): Result<Person> {
            val rString = propertyReader.readAsPropertyString(propertyName)
            val rPropReader = rString.map { stringPropertyReader(it) }
            return rPropReader.flatMap { readPerson(it) }
        }

        fun readAsPersonList(propertyName: String, propertyReader: PropertyReader): Result<List<Person>> =
            propertyReader.readAsList(propertyName) { it }.flatMap { list ->
                sequence(list.map { s ->
                    readPerson(stringPropertyReader(PropertyReader.toPropertyString(s)))
                })
            }

        private fun readPerson(propertyReader: PropertyReader): Result<Person> =
            propertyReader.readAsInt("id")
                .flatMap { id ->
                    propertyReader.readAsString("firstName")
                        .flatMap { firstName ->
                            propertyReader.readAsString("lastName")
                                .flatMap { lastName -> Person.of(id, firstName, lastName) }
                        }
                }
    }
}

fun <T> assertCondition(value: T, f: (T) -> Boolean): Result<T> =
    assertCondition(value, f, "Assertion error: condition should evaluate to true")

fun <T> assertCondition(value: T, f: (T) -> Boolean, message: String): Result<T> =
    if (f(value))
        Result(value)
    else
        Result.failure(IllegalStateException(message))

fun assertTrue(condition: Boolean, message: String = "Assertion error: condition should be true"): Result<Boolean> =
    assertCondition(condition, { x -> x }, message)

fun assertFalse(condition: Boolean, message: String = "Assertion error: condition should be false"): Result<Boolean> =
    assertCondition(condition, { x -> !x }, message)

fun <T> assertNotNull(t: T?): Result<T> =
    assertNotNull(t, "Assertion error: object should not be null")

fun <T> assertNotNull(t: T?, message: String): Result<T> =
    if (t != null)
        Result(t)
    else
        Result.failure(IllegalStateException(message))

fun assertPositive(value: Int, message: String = "Assertion error: value $value must be positive"): Result<Int> =
    assertCondition(value, { x -> x > 0 }, message)

fun assertInRange(value: Int, min: Int, max: Int): Result<Int> =
    assertCondition(value, { x -> x in min .. max - 1 },
        "Assertion error: value $value should be > $min and < $max")

fun assertPositiveOrZero(value: Int, message: String = "Assertion error: value $value must not be < 0"): Result<Int> =
    assertCondition(value, { x -> x >= 0 }, message)

fun <A: Any> assertType(element: A, clazz: Class<*>): Result<A> =
    assertType(element, clazz, "Wrong type: ${element.javaClass}, expected: ${clazz.name}")

fun <A: Any> assertType(element: A, clazz: Class<*>, message: String): Result<A> =
    assertCondition(element, { e -> e.javaClass == clazz }, message)