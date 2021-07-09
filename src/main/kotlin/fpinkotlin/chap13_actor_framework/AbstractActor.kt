package fpinkotlin.chap13_actor_framework

import fpinkotlin.chap13_actor_framework.common.*
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

abstract class AbstractActor<T>(protected val id: String): Actor<T> {
    override val context: ActorContext<T> = object: ActorContext<T> {
        var behavior: MessageProcessor<T> = object: MessageProcessor<T> {
            override fun process(message: T, sender: Result<Actor<T>>) {
                onReceive(message, sender)
            }
        }

        @Synchronized
        override fun become(behavior: MessageProcessor<T>) {
            this.behavior = behavior
        }

        override fun behavior(): MessageProcessor<T> = behavior
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor(DaemonThreadFactory())

    abstract fun onReceive(message: T, sender: Result<Actor<T>>)

    override fun self(): Result<Actor<T>> {
        return Result(this)
    }

    override fun shutdown() {
        this.executor.shutdown()
    }

    @Synchronized
    override fun tell(message: T, sender: Result<Actor<T>>) {
        executor.execute {
            try {
                context.behavior()
                    .process(message, sender)
            } catch (e: RejectedExecutionException) {
                /*
                 * This is probably normal and means all pending tasks
                 * were canceled because the actor was stopped.
                 */
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}