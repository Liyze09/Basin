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
package net.liyze.basin

import net.liyze.basin.core.start
import net.liyze.basin.event.EventBus
import net.liyze.basin.graal.GraalPolyglot
import net.liyze.basin.http.HttpServer
import net.liyze.basin.rpc.RpcService

object BasinFramework {
    @JvmField
    val httpServer = HttpServer

    @JvmField
    val rpcServer = RpcService

    @JvmField
    val eventBus = EventBus
    fun startBasin() {
        start()
        RpcService.start()
        HttpServer.start()
    }

    fun loadScript(vararg path: String) {
        try {
            Class.forName("org.graalvm.polyglot.Context")
        } catch (_: Exception) {
            throw UnsupportedOperationException("Graal polyglot engine not found!")
        }
        path.forEach {
            GraalPolyglot.loadScript(it)
        }
    }
}