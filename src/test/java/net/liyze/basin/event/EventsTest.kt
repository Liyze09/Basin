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

import net.liyze.basin.BasinFramework
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class EventsTest {
    @Disabled
    @Test
    fun test() {
        val eventBus = BasinFramework.getDefaultArticle().eventBus
        val event = Any()
        eventBus.enableAgent = true
        eventBus.asyncSubscribe(event) {
            Thread.sleep(20)
            println("get")
        }
        repeat(1000) {
            eventBus.emit(event, Any())
            println("emit")
        }
        Thread.sleep(500)
        println(eventBus.agentResult)
    }
}