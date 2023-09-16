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

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

object EventLoop {
    internal val schedule = Executors.newScheduledThreadPool(2)
    private val events: MutableList<EventAndCondition> = CopyOnWriteArrayList()
    var loopPeriod: Long = 15
    var maxBufferSize: Int = 64
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