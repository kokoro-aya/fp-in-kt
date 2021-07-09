package fpinkotlin.chap13_actor_framework

import fpinkotlin.chap13_actor_framework.common.*

interface Actor<T> {
    val context: ActorContext<T>
    fun self(): Result<Actor<T>> = Result(this)
    fun tell(message: T, sender: Result<Actor<T>> = self())
    fun shutdown()
    fun tell(message: T, sender: Actor<T>) = tell(message, Result(sender))

    companion object {
        fun <T> noSender(): Result<Actor<T>> = Result()
    }
}

interface ActorContext<T> {
    fun behavior(): MessageProcessor<T>
    fun become(behavior: MessageProcessor<T>)
}

interface MessageProcessor<T> {
    fun process(message: T, sender: Result<Actor<T>>)
}