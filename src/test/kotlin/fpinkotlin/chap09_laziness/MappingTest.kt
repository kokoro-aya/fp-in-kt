package fpinkotlin.chap09_laziness

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

class MappingTest {
    private val stdOut = System.out
    private val outputCaptor = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outputCaptor))
    }

    @Test
    fun testMap() {
        val greets: (String) -> String = { "Hello, $it!" }
        val name: Lazy<String> = Lazy {
            println("Evaluating name")
            "Mickey"
        }
        val defaultMessage = Lazy {
            println("Evaluating default message")
            "No greetings when time is odd"
        }

        val message = name.map(greets)
        val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0

        println(if (condition) message() else defaultMessage())
        println(if (condition) message() else defaultMessage())
        println(if (condition) message() else defaultMessage())

        val holds = """
            Evaluating name
            Hello, Mickey!
            Hello, Mickey!
            Hello, Mickey!
        """.trimIndent()

        val fail = """
            Evaluating default message
            No greetings when time is odd
            No greetings when time is odd
            No greetings when time is odd
        """.trimIndent()

        assertThat(outputCaptor.toString().trim()).isIn(fail, holds)
    }

    @Test
    fun testFlatMap() {

        fun getGreetings(locale: Locale): String {
            println("Evaluating greetings")
            Thread.sleep(250L)
            return "Hello"
        }

        val greetings: Lazy<String> = Lazy { getGreetings(Locale.US) }

        val flatGreets: (String) -> Lazy<String> = { name -> greetings.map { "$it, $name!" } }

        val name: Lazy<String> = Lazy {
            println("Evaluating name")
            "Mickey"
        }

        val defaultMessage = Lazy {
            println("Evaluating default message")
            "No greetings when time is odd"
        }

        val message = name.flatMap(flatGreets)
        val condition = Random(System.currentTimeMillis()).nextInt() % 2 == 0
        println(if (condition) message() else defaultMessage())
        println(if (condition) message() else defaultMessage())

        val holds = """
            Evaluating name
            Evaluating greetings
            Hello, Mickey!
            Hello, Mickey!
        """.trimIndent()

        val fail = """
            Evaluating default message
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