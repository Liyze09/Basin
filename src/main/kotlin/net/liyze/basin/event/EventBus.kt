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

import net.liyze.basin.event.exception.ConnectFailedException
import net.liyze.basin.event.exception.NoSuchObserverException
import net.liyze.basin.http.HttpServer
import net.liyze.basin.rpc.RpcService
import net.liyze.basin.rpc.request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

object EventBus {
    private val LOGGER: Logger = LoggerFactory.getLogger(EventBus::class.java)
    private val remotes: MutableList<String> = Vector()
    private val buffer: Queue<EventAndMessage> = ConcurrentLinkedQueue()
    private var delay: Long = 0
    fun connect(url: String) {
        if (request(url, "_hello", Hello()).await() == 1) remotes.add(url)
        else throw ConnectFailedException(url)
    }

    init {
        RpcService.subscribe("_hello") {
            if (it is Hello) return@subscribe 1
            LOGGER.warn("Illegal '_hello' request!")
            return@subscribe 255
        }
        RpcService.subscribe("_emit") {
            if (it is EventAndMessage) {
                remoteEmit(it)
                return@subscribe 1
            }
            LOGGER.warn("Illegal '_emit' request!")
            return@subscribe 255

        }
        EventLoop.schedule.scheduleAtFixedRate({
            val element = buffer.poll()
            if (element != null) {
                map[element.event]?.run(element.message)
            }
        }, 0, EventLoop.loopPeriod, TimeUnit.MILLISECONDS)
    }

    val eventLoop = EventLoop
    val httpServer = HttpServer
    val rpcServer = RpcService
    private val map: MutableMap<Any, Observer> = HashMap()
    fun subscribe(event: Any, observer: Observer) {
        LOGGER.debug("An observer is subscribing.")
        map[event] = observer
    }

    fun asyncSubscribe(event: Any, observer: Observer) {
        LOGGER.debug("An async observer is subscribing.")
        map[event] = Observer {
            Thread.ofVirtual().start {
                observer.run(it)
            }
        }
    }

    fun publish(event: Any, condition: Condition): Int {
        return eventLoop.publish(event, condition)
    }

    fun cancelPublish(id: Int) {
        eventLoop.cancel(id)
    }

    fun override(event: Any, eventAop: EventAop) {
        LOGGER.debug("An aop observer is subscribing.")
        val observer = map[event] ?: throw NoSuchObserverException(event.toString())
        map[event] = Observer {
            eventAop.proxy(observer, it)
        }
    }

    fun emit(event: Any, message: Any) {
        val wait = delay - System.currentTimeMillis()
        if (wait > 0) {
            Thread.sleep(wait)
        }
        val em = EventAndMessage(event, message)
        buffer.add(em)
        remotes.forEach {
            request(it, "_emit", em)
        }
        if (buffer.size >= eventLoop.maxBufferSize) {
            delay = System.currentTimeMillis() + eventLoop.loopPeriod
        }
    }

    private fun remoteEmit(eventAndMessage: EventAndMessage) {
        buffer.add(eventAndMessage)
    }

    private class Hello
    private data class EventAndMessage(val event: Any, val message: Any)
}