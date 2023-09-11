@file: JvmName("BasinFramework")

package net.liyze.basin

import net.liyze.basin.core.start
import net.liyze.basin.event.EventBus
import net.liyze.basin.http.HttpServer
import net.liyze.basin.rpc.RpcService

val httpServer = HttpServer
val rpcServer = RpcService
val eventBus = EventBus
fun startBasin() {
    start()
}