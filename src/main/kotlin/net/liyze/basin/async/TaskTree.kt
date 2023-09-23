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

package net.liyze.basin.async

import java.util.concurrent.ConcurrentHashMap

class TaskTree {
    private val base: TaskMeta = TaskMeta()
    private val map: MutableMap<String, TaskMeta>

    init {
        base.task = Task {}
        base.name = "base"
        map = ConcurrentHashMap(mapOf("base" to base))
    }

    @JvmOverloads
    fun fork(name: String, parent: String = "base", task: Task): TaskTree {
        val current = TaskMeta()
        current.name = name
        current.task = task
        map[parent]?.then?.add(current)
        map[name] = current
        return this
    }

    fun start() {
        Thread.ofVirtual().start { base.start(Context(ConcurrentHashMap())) }
    }
}