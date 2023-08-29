@file: JvmName("BRpcStarter")

package net.liyze.basin.rpc

import net.liyze.basin.core.Server

private var server: Server? = null
fun startRpcServer(port: Int) {
    if (server == null) {
        server = RpcServer(port).start()
    }
}
