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

import net.liyze.basin.core.Config
import net.liyze.basin.core.start
import net.liyze.basin.event.EventBus
import net.liyze.basin.graal.Polyglot
import net.liyze.basin.http.HttpServer
import net.liyze.basin.rpc.RpcService
import net.liyze.basin.util.createInstance
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object BasinFramework {
    @JvmField
    val httpServer = HttpServer

    @JvmField
    val rpcServer = RpcService

    @JvmField
    val eventBus = EventBus

    @JvmField
    val config = Config
    fun startBasin() {
        start()
        RpcService.start()
        HttpServer.start()
    }

    private var polyglot: Polyglot? = null
    fun loadScript(vararg path: String) {
        path.forEach {
            polyglot?.loadScript(it) ?: throw IllegalStateException("Must init polyglot engine before use!")
        }
    }

    fun initPolyglot() {
        try {
            Class.forName("org.graalvm.polyglot.Context")
        } catch (_: Exception) {
            throw UnsupportedOperationException("GraalVM polyglot engine not found!")
        }
        polyglot = Class.forName("net.liyze.basin.graal.GraalPolyglot").createInstance() as Polyglot
    }

    val threadPool: ExecutorService = Executors.newCachedThreadPool()
    fun run(run: Runnable) {
        threadPool.submit(run)
    }
}