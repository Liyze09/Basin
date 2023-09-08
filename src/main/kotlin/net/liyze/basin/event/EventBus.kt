package net.liyze.basin.event

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.liyze.basin.http.HttpServer
import net.liyze.basin.rpc.RpcService

object EventBus {
    val eventLoop = EventLoop
    val httpServer = HttpServer
    val rpcServer = RpcService
    private val map: MutableMap<Any, Observer> = HashMap()
    fun subscribe(event: Any, observer: Observer) {
        map[event] = observer
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun asyncSubscribe(event: Any, observer: Observer) {
        map[event] = Observer {
            GlobalScope.launch {
                observer.run(it)
            }
        }
    }

    fun publish(event: Any, condition: Condition) {
        eventLoop.publish(event, condition)
    }

    fun cancelPublish(event: Any, condition: Condition) {
        eventLoop.cancel(event, condition)
    }

    fun emit(event: Any) {
        map[event]?.run(event)
    }
}