package fpinkotlin.chap13_actor_framework

import fpinkotlin.chap13_actor_framework.common.Result
import java.util.concurrent.Semaphore

private class Player(id: String, private val sound: String, private val referee: Actor<Int>): AbstractActor<Int>(id) {
    override fun onReceive(message: Int, sender: Result<Actor<Int>>) {
        println("$sound - $message")
        if (message >= 10) {
            referee.tell(message, sender)
        } else {
            sender.forEach({ actor: Actor<Int> ->
                actor.tell(message + 1, self())
            }, { referee.tell(message, sender) })
        }
    }
}

private val semaphore = Semaphore(1)

fun main() {
    val referee = object: AbstractActor<Int>("Referee") {
        override fun onReceive(message: Int, sender: Result<Actor<Int>>) {
            println("Game ended after $message shots")
            semaphore.release()
        }
    }

    val player1 = Player("Player1", "ping", referee)
    val player2 = Player("Player2", "pong", referee)

    semaphore.acquire()
    player1.tell(1, Result(player2))
    semaphore.acquire()
}