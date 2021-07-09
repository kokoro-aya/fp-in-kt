package fpinkotlin.chap13_actor_framework

import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

class DaemonThreadFactory : ThreadFactory {

    override fun newThread(runnableTask: Runnable): Thread {
        val thread = Executors.defaultThreadFactory().newThread(runnableTask)
        thread.isDaemon = true
        return thread
    }
}