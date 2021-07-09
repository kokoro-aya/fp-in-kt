package fpinkotlin.chap13_actor_framework

import fpinkotlin.chap13_actor_framework.common.Heap
import fpinkotlin.chap13_actor_framework.common.List
import fpinkotlin.chap13_actor_framework.common.Result
import fpinkotlin.chap13_actor_framework.common.range
import java.util.concurrent.Semaphore

class Worker(id: String): AbstractActor<Pair<Int, Int>>(id) {
    override fun onReceive(message: Pair<Int, Int>, sender: Result<Actor<Pair<Int, Int>>>) {
        sender.forEach(onSuccess = { a: Actor<Pair<Int, Int>> ->
            a.tell(Pair(slowFibonacci(message.first), message.second), self())})
    }

    private fun slowFibonacci(number: Int): Int {
        return when (number) {
            0 -> 1
            1 -> 1
            else -> slowFibonacci(number - 1) + slowFibonacci(number - 2)
        }
    }
}

class Manager(id: String, list: List<Int>,
              private val client: Actor<Result<List<Int>>>,
              private val workers: Int): AbstractActor<Pair<Int, Int>>(id) {
    private val initial: List<Pair<Int, Int>>
    private val workList: List<Pair<Int, Int>>
    private val resultHeap: Heap<Pair<Int, Int>>
    private val managerFunction: (Manager) -> (Behavior) -> (Pair<Int, Int>) -> Unit

    init {
        val splitLists = list.zipWithPosition().splitAt(this.workers)
        this.initial = splitLists.first
        this.workList = splitLists.second
        this.resultHeap = Heap(Comparator {
            p1: Pair<Int, Int>, p2: Pair<Int, Int> -> p1.second.compareTo(p2.second)
        })

        managerFunction = { manager ->
            { behavior ->
                { p ->
                    val result = behavior.resultHeap + p
                    if (result.size == list.length) {
                        this.client.tell(Result(result.toList().map { it.first }))
                    } else {
                        manager.context
                            .become(Behavior(behavior.workList
                                .tailSafe()
                                .getOrElse(List()), result))
                    }
                }
            }
        }
    }

    internal inner class Behavior internal constructor(
        internal val workList: List<Pair<Int, Int>>,
        internal val resultHeap: Heap<Pair<Int, Int>>): MessageProcessor<Pair<Int, Int>> {
        override fun process(message: Pair<Int, Int>, sender: Result<Actor<Pair<Int, Int>>>) {
            managerFunction(this@Manager)(this@Behavior)(message)
            sender.forEach(onSuccess = { a: Actor<Pair<Int, Int>> ->
                workList.headSafe()
                    .forEach({ a.tell(it, self()) }) { a.shutdown() }
            })
        }
    }

    fun start() {
        onReceive(0 to 0, self())
        fpinkotlin.chap13_actor_framework.common.sequence(initial.map { this.initWorker(it) })
            .forEach(onSuccess = { this.initWorkers(it) },
                     onFailure = {
                         this.tellClientEmptyResult(it.message ?: "Unknown error")})

    }

    private fun initWorker(t: Pair<Int, Int>): Result<() -> Unit> =
        Result({ Worker("Worker " + t.second).tell(t.first to t.second, self()) })

    private fun initWorkers(lst: List<() -> Unit>) {
        lst.forEach { it() }
    }

    private fun tellClientEmptyResult(string: String) {
        client.tell(Result.failure("$string caused by empty input list."))
    }

    override fun onReceive(message: Pair<Int, Int>, sender: Result<Actor<Pair<Int, Int>>>) {
        context.become(Behavior(workList, resultHeap))
    }
}

private val semaphore = Semaphore(1)
private const val listLength = 20_000
private const val workers = 8
private val rnd = java.util.Random(0)
private val testList = range(0, listLength).map { rnd.nextInt(35) }

fun main() {
    semaphore.acquire()
    val startTime = System.currentTimeMillis()
    val client =
        object : AbstractActor<Result<List<Int>>>("Client") {
            override fun onReceive(message: Result<List<Int>>,
                                   sender: Result<Actor<Result<List<Int>>>>) {
                message.forEach( { processSuccess(it) }, { processFailure(it.message ?: "Unknown error") })
                println("Total time: " + (System.currentTimeMillis() - startTime))
                semaphore.release()
            }
        }
    val manager = Manager("Manager", testList, client, workers)
    manager.start()
    semaphore.acquire()
}

private fun processFailure(message: String) {
    println(message)
}

private fun processSuccess(lst: List<Int>) {
    println("Input: ${testList.splitAt(40).first}")
    println("Result: ${lst.splitAt(40).first}")
}