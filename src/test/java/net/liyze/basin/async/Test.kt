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

import net.liyze.basin.util.deepClone
import org.junit.jupiter.api.Test

class Test {
    @Test
    fun deepCloneTest() {
        val any = Context()
        repeat(1000000) {
            any.deepClone()
        }
    }

    @Test
    fun taskTreeTest() {
        val tree = TaskTree()
        tree.fork(
            name = "task1"
        ) {
            println("task1")
            Any()
        }
        tree.fork(
            name = "task2",
            parent = "task1"
        ) {
            println("task2")
            println(it.get().toString())
        }
        tree.fork(
            name = "task3",
            parent = "task1"
        ) {
            println("task3")
            println(it.get().toString())
        }
        tree.start()
        Thread.sleep(800)
    }
}