package net.liyze.basin.event

import java.util.concurrent.*

object EventLoop {
    private val schedule = Executors.newScheduledThreadPool(0)

    init {
        (schedule as ScheduledThreadPoolExecutor).removeOnCancelPolicy = true
    }

    private val map: MutableMap<EventAndCondition, ScheduledFuture<*>> = ConcurrentHashMap()
    fun publish(event: Any, condition: Condition) {
        map[EventAndCondition(event, condition)] = schedule.scheduleAtFixedRate(
            {
                if (condition.test()) {
                    EventBus.emit(event)
                }
            }, 0, 10, TimeUnit.MILLISECONDS
        )
    }

    fun cancel(event: Any, condition: Condition) {
        map[EventAndCondition(event, condition)]?.cancel(false)
    }

    data class EventAndCondition(val event: Any, val condition: Condition)
}