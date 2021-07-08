package fpinkotlin.chap09_laziness

import fpinkotlin.chap08_advanced_lists.MList
import org.assertj.core.api.Assertions.anyOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.util.*
import kotlin.test.assertIs

class ComposingTest {
    private val stdOut = System.out
    private val outputCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outputCaptor))
    }

    @Test
    fun testSequence() {
        val name1: Lazy<String> = Lazy {
            println("Evaluating name1")
            "Mickey"
        }

        val name2: Lazy<String> = Lazy {
            println("Evaluating name2")
            "Donald"
        }

        val name3 = Lazy {
            println("Evaluating name3")
            "Goofy"
        }

        val list = sequence(MList(name1, name2, name3))
        val defaultMessage = "No greetings when time is odd"
        val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0

        println(if (condition) list() else defaultMessage)
        println(if (condition) list() else defaultMessage)

        val holds = """
            Evaluating name1
            Evaluating name2
            Evaluating name3
            [Mickey -> Donald -> Goofy -> NIL]
            [Mickey -> Donald -> Goofy -> NIL]
        """.trimIndent()

        val fail = """
            No greetings when time is odd
            No greetings when time is odd
        """.trimIndent()

        assertThat(outputCaptor.toString().trim()).isIn(fail, holds)
    }

    @Test
    fun testSequenceResult() {
        val name1: Lazy<String> = Lazy {
            println("Evaluating name1")
            "Mickey"
        }

        val name2: Lazy<String> = Lazy {
            println("Evaluating name2")
            "Donald"
        }

        val name3 = Lazy {
            println("Evaluating name3")
            "Goofy"
        }

        val name4 = Lazy {
            println("Evaluating name4")
            throw IllegalStateException("Exception while evaluating name4")
        }

        val list1 = sequenceResult(MList(name1, name2, name3))
        val list2 = sequenceResult(MList(name1, name2, name3, name4))
        val defaultMessage = "No greetings when time is odd"
        val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0

        println(if (condition) list1() else defaultMessage)
        println(if (condition) list1() else defaultMessage)
        println(if (condition) list2() else defaultMessage)
        println(if (condition) list2() else defaultMessage)

        val holds = """
            Evaluating name1
            Evaluating name2
            Evaluating name3
            Success([Goofy -> Donald -> Mickey -> NIL])
            Success([Goofy -> Donald -> Mickey -> NIL])
            Evaluating name4
            Failure(class java.lang.IllegalStateException:Exception while evaluating name4)
            Failure(class java.lang.IllegalStateException:Exception while evaluating name4)
        """.trimIndent()

        val fail = """
            No greetings when time is odd
            No greetings when time is odd
            No greetings when time is odd
            No greetings when time is odd
        """.trimIndent()

        assertThat(outputCaptor.toString().trim()).isIn(fail, holds)
    }

    @AfterEach
    fun tearDown() {
        System.setOut(stdOut)
    }
}