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
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object EventBus {
    private val LOGGER: Logger = LoggerFactory.getLogger(EventBus::class.java)
    private val remotes: MutableList<String> = CopyOnWriteArrayList()
    fun getRemotes() = remotes
    var enableAgent = false
    private val buffer = ArrayBlockingQueue<EventAndMessage>(EventLoop.maxBufferSize)
    val agentResult: MutableMap<Any, Agent> = ConcurrentHashMap()

    data class Agent(var callTime: Long, var avgTime: Long)
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
                Thread.ofVirtual().start { remoteEmit(it) }
                return@subscribe 1
            }
            LOGGER.warn("Illegal '_emit' request!")
            return@subscribe 255

        }
        Thread.ofVirtual().start {
            while (!Thread.interrupted()) {
                val element = buffer.take()
                map[element.event]?.run(element.message)
            }
        }
    }

    val eventLoop = EventLoop
    val httpServer = HttpServer
    val rpcServer = RpcService
    private val map: MutableMap<Any, Observer> = HashMap()
    fun subscribe(event: Any, observer: Observer) {
        LOGGER.debug("An observer is subscribing.")
        var obs = observer
        if (enableAgent) {
            obs = addAgent(event, obs)
        }
        map[event] = obs
        agentResult[event] = Agent(0, 0)
    }

    fun asyncSubscribe(event: Any, observer: Observer) {
        LOGGER.debug("An async observer is subscribing.")
        var obs = observer
        if (enableAgent) {
            obs = addAgent(event, obs)
        }
        map[event] = Observer {
            Thread.ofVirtual().start {
                obs.run(it)
            }
        }
        agentResult[event] = Agent(0, 0)
    }

    fun addAgent(event: Any, observer: Observer): Observer {
        return Observer {
            val t0 = System.nanoTime()
            observer.run(it)
            val t1 = System.nanoTime() - t0
            val e = agentResult[event]!!
            val ct = e.callTime
            val nct = ct + 1
            var avg = e.avgTime
            avg *= ct
            avg += t1
            avg /= nct
            agentResult[event]!!.callTime = nct
            agentResult[event]!!.avgTime = avg
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
        val em = EventAndMessage(event, message)
        buffer.put(em)
        remotes.forEach {
            request(it, "_emit", em)
        }
    }

    @JvmOverloads
    fun defineClusterRelay(topic: Any, vararg targets: String = remotes.toTypedArray()) {
        val cluster = Cluster(topic, targets.toList())
        subscribe(topic) {
            cluster.whenReceive(it)
        }
    }

    @JvmOverloads
    fun defineRelay(topic: Any, vararg targets: String = remotes.toTypedArray()) {
        subscribe(topic) { msg ->
            targets.forEach {
                request(it, "_emit", EventAndMessage(topic, msg))
            }
        }
    }

    private fun remoteEmit(eventAndMessage: EventAndMessage) {
        buffer.put(eventAndMessage)
    }

    private class Hello
    private data class EventAndMessage(val event: Any, val message: Any)
    private data class Cluster(val topic: Any, val target: List<String>) {
        @Volatile
        var currentTarget = 0

        @Synchronized
        fun whenReceive(msg: Any) {
            request(target[currentTarget], "_emit", EventAndMessage(topic, msg))
            if (currentTarget == target.size - 1) {
                currentTarget = 0
            }
            currentTarget++
        }
    }
}