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

import net.liyze.basin.async.exception.IllegalOriginThreadException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

abstract class AbstractDataFlow<T>(
    bufferSize: Int = 0x7fffffff
) {
    internal val queue: BlockingQueue<T> = LinkedBlockingQueue(bufferSize)
    abstract fun put(value: T)
    fun get(): T = queue.take()
}

class DataFlow<T>(
    bufferSize: Int = 0x7fffffff
) : AbstractDataFlow<T>(bufferSize) {
    val sendThread: Thread = Thread.currentThread()
    override fun put(value: T) {
        val current = Thread.currentThread()
        if (sendThread != current) {
            throw IllegalOriginThreadException(current)
        }
        queue.put(value!!)
    }
}