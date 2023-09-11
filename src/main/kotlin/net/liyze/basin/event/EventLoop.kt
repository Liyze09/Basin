package net.liyze.basin.event

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object EventLoop {
    internal val schedule = Executors.newScheduledThreadPool(2)
    private val events: MutableList<EventAndCondition> = CopyOnWriteArrayList()
    var loopPeriod: Long = 15

    init {
        (schedule as ScheduledThreadPoolExecutor).removeOnCancelPolicy = true
        schedule.scheduleAtFixedRate({
            events.forEach {
                if (it.condition.test()) {
                    EventBus.emit(it.event, it.condition)
                }
            }
        }, 0, loopPeriod, TimeUnit.MILLISECONDS)
    }

    fun publish(event: Any, condition: Condition): Int {
        events.add(EventAndCondition(event, condition))
        return events.size - 1
    }

    fun cancel(id: Int) {
        events.removeAt(id)
    }

    private data class EventAndCondition(val event: Any, val condition: Condition)
}