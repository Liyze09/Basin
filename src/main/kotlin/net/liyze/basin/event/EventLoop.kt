/*
 * Copyright (c) 2023 Liyze09
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liyze.basin.event

import net.liyze.basin.Article
import net.liyze.basin.core.Config
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class EventLoop(context: Article) {
    internal val schedule = Executors.newScheduledThreadPool(2)
    private val events: MutableList<EventAndCondition> = CopyOnWriteArrayList()
    private val removed: MutableList<Int> = ArrayList()
    var loopPeriod: Long = Config.loopPeriod
    var maxBufferSize: Int = Config.maxBufferSize

    init {
        (schedule as ScheduledThreadPoolExecutor).removeOnCancelPolicy = true
        schedule.scheduleAtFixedRate({
            for ((index, it) in events.withIndex()) {
                if (it.condition.test()) {
                    context.eventBus.emit(it.event, it.condition)
                    if (it.isOnce) {
                        removed.add(index)
                    }
                }
            }
            removed.forEach {
                events.removeAt(it)
            }
        }, 0, loopPeriod, TimeUnit.MILLISECONDS)
    }

    fun publish(event: Any, condition: Condition): Int {
        events.add(EventAndCondition(event, condition))
        return events.size - 1
    }

    fun publishOnce(event: Any, condition: Condition): Int {
        events.add(EventAndCondition(event, condition, true))
        return events.size - 1
    }

    fun cancel(id: Int) {
        events.removeAt(id)
    }

    private data class EventAndCondition(val event: Any, val condition: Condition, val isOnce: Boolean = false)
}