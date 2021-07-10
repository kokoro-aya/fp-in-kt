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
              private val receiver: Actor<Int>,
              private val workers: Int): AbstractActor<Pair<Int, Int>>(id) {
    private val initial: List<Pair<Int, Int>>
    private val workList: List<Pair<Int, Int>>
    private val resultHeap: Heap<Pair<Int, Int>>
    private val managerFunction: (Manager) -> (Behavior) -> (Pair<Int, Int>) -> Unit

    private val limit: Int

    init {
        val splitLists = list.zipWithPosition().splitAt(this.workers)
        this.initial = splitLists.first
        this.workList = splitLists.second
        this.resultHeap = Heap(Comparator {
            p1: Pair<Int, Int>, p2: Pair<Int, Int> -> p1.second.compareTo(p2.second)
        })

        limit = workList.length

        managerFunction = { manager ->
            { behavior ->
                { p ->
                    val result = streamResult(behavior.resultHeap + p, behavior.expected, List())
                    result.third.forEach { receiver.tell(it) }
                    if (result.second > limit) {
                        this.receiver.tell(-1)
                    } else {
                        manager.context
                            .become(Behavior(behavior.workList
                                .tailSafe()
                                .getOrElse(List()), result.first, result.second))
                    }
                }
            }
        }
    }

    private fun streamResult(result: Heap<Pair<Int, Int>>, expected: Int, list: List<Int>): Triple<Heap<Pair<Int, Int>>, Int, List<Int>> {
        val triple = Triple(result, expected, list)
        val temp = result.head
            .flatMap { head ->
                result.tail().map { tail ->
                    if (head.second == expected)
                        streamResult(tail, expected + 1, list.cons(head.first))
                    else
                        triple
                }
            }
        return temp.getOrElse(triple)
    }

    internal inner class Behavior internal constructor(
        internal val workList: List<Pair<Int, Int>>,
        internal val resultHeap: Heap<Pair<Int, Int>>,
        internal val expected: Int): MessageProcessor<Pair<Int, Int>> {
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
                     onFailure = { receiver.tell(-1) })

    }

    private fun initWorker(t: Pair<Int, Int>): Result<() -> Unit> =
        Result({ Worker("Worker " + t.second).tell(t.first to t.second, self()) })

    private fun initWorkers(lst: List<() -> Unit>) {
        lst.forEach { it() }
    }

//    private fun tellClientEmptyResult(string: String) {
//        receiver.tell(Result.failure("$string caused by empty input list."))
//    }

    override fun onReceive(message: Pair<Int, Int>, sender: Result<Actor<Pair<Int, Int>>>) {
        context.become(Behavior(workList, resultHeap, 0))
    }
}

class Receiver(id: String, private val client: Actor<List<Int>>): AbstractActor<Int>(id) {
    private val receiverFunction: (Receiver) -> (Behavior) -> (Int) -> Unit

    init {
        receiverFunction = { receiver ->
            { behavior ->
                { i ->
                    if (i == -1) {
                        this.client.tell(behavior.resultList.reverse())
                        shutdown()
                    } else {
                        receiver.context
                            .become(Behavior(behavior.resultList.cons(i)))
                    }
                }
            }
        }
    }

    override fun onReceive(i: Int, sender: Result<Actor<Int>>) {
        context.become(Behavior(List(i)))
    }

    internal inner class Behavior internal constructor(internal val resultList: List<Int>): MessageProcessor<Int> {
        override fun process(i: Int, sender: Result<Actor<Int>>) {
            receiverFunction(this@Receiver)(this@Behavior)(i)
        }
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
        object : AbstractActor<List<Int>>("Client") {
            override fun onReceive(message: List<Int>,
                                   sender: Result<Actor<List<Int>>>) {
                println("Input: ${testList.splitAt(40).first}")
                println("Result: ${message.splitAt(40).first}")
                println("Total time: " + (System.currentTimeMillis() - startTime))
                semaphore.release()
            }
        }
    val receiver = Receiver("Receiver", client)
    val manager = Manager("Manager", testList, receiver, workers)
    manager.start()
    semaphore.acquire()
}
