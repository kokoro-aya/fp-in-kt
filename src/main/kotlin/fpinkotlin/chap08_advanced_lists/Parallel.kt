package fpinkotlin.chap08_advanced_lists

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import fpinkotlin.chap07_exceptions.Result
import java.math.BigInteger
import java.util.*
import java.util.concurrent.Executors

fun <A> MList<A>.divide(depth: Int): MList<MList<A>> {
    tailrec fun divide(list: MList<MList<A>>, depth: Int): MList<MList<A>> = when (list) {
        is MList.Nil -> list // dead code
        is MList.Cons ->
            if (list.head.length < 2 || depth < 1)
                list
            else
                divide(list.flatMap { x -> x.splitListAt(x.length / 2) }, depth - 1)
    }

    return if (this.isEmpty()) MList(this) else divide(MList(this), depth)
}

fun <A> MList<A>.splitListAt(index: Int): MList<MList<A>> {
    tailrec fun splitListAt_(acc: MList<A>, list: MList<A>, i: Int): MList<MList<A>> = when (list) {
        is MList.Nil -> MList(list.reverse(), acc)
        is MList.Cons ->
            if (i == 0) MList(list.reverse(), acc) else splitListAt_(acc.cons(list.head), list.tail, i - 1)
    }
    return when {
        index < 0 -> splitListAt(0)
        index > length -> splitListAt(length)
        else -> splitListAt_(MList(), this.reverse(), this.length - index)
    }
}

fun <A, B> MList<A>.parFoldLeft(es: ExecutorService, identity: B,
                                f: (B) -> (A) -> B, m: (B) -> (B) -> B): Result<B> =
    try {
        val res: MList<B> = divide(1024).map { list: MList<A> ->
            es.submit<B> { list.foldLeft(identity, f) }
        }.map { fb ->
            try {
                fb.get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }
        Result(res.foldLeft(identity, m))
    } catch (e: Exception) {
        Result.failure(e)
    }

fun <A, B> MList<A>.parMap(es: ExecutorService, g: (A) -> B): Result<MList<B>> =
    try {
        val result = this.map { x -> es.submit<B> { g(x) } }.map { cc ->
            try {
                cc.get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }
        }
        Result(result)
    } catch (e: Exception) {
        Result.failure(e)
    }

/**
 * Copied from the accompanying code with minor adaptations (https://github.com/pysaumont/fpinkotlin)
 */
object SerialParallelFoldLeftBenchmark {

    private val random = Random()

    @JvmStatic
    fun main(args: Array<String>) {
        val testLimit = 35000

        val testList: MList<Long> = range(0, testLimit).map {
            random.nextInt(30).toLong()
        }

        val es2 = Executors.newFixedThreadPool(2)
        val es4 = Executors.newFixedThreadPool(4)
        val es8 = Executors.newFixedThreadPool(8)

        testSerial(5, testList, System.currentTimeMillis())
        println("Duration serial 1 thread: ${testSerial(10, testList, System.currentTimeMillis())}")
        testParallel(es2, 5, testList, System.currentTimeMillis())
        println("Duration parallel 2 threads: ${testParallel(es2, 10, testList, System.currentTimeMillis())}")
        testParallel(es4, 5, testList, System.currentTimeMillis())
        println("Duration parallel 4 threads: ${testParallel(es4, 10, testList, System.currentTimeMillis())}")
        testParallel(es8, 5, testList, System.currentTimeMillis())
        println("Duration parallel 8 threads: ${testParallel(es8, 10, testList, System.currentTimeMillis())}")
        es2.shutdown()
        es4.shutdown()
        es8.shutdown()
    }

    private val f = { a: BigInteger -> { b: Long -> a.add(BigInteger.valueOf(fibo(b))) } }
    private val g = { a: BigInteger -> { b: BigInteger -> a.add(b) } }

    private fun testSerial(n: Int, list: MList<Long>, startTime: Long): Long {
        (0 until n).forEach { _ ->
            println("Result:  ${list.foldLeft(BigInteger.ZERO, f)}")
        }
        return System.currentTimeMillis() - startTime
    }

    private fun testParallel(es: ExecutorService, n: Int, list: MList<Long>, startTime: Long): Long {
        (0 until n).forEach { _ ->
            list.parFoldLeft(es, BigInteger.ZERO, f, g).forEachOrElse({ println("Result: $it") },
                { println("Exception:  ${it.message}") },
                { println("Empty result") })
        }
        return System.currentTimeMillis() - startTime
    }

    private fun fibo(x: Long): Long {
        return when (x) {
            0L -> 0
            1L -> 1
            else -> fibo(x - 1) + fibo(x - 2)
        }
    }
}