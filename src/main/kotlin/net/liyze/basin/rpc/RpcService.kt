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

package net.liyze.basin.rpc

import io.fury.Fury
import io.fury.Language
import io.fury.ThreadLocalFury
import net.liyze.basin.async.Context
import net.liyze.basin.core.Server
import net.liyze.basin.event.RpcObserver
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.IOException

object RpcService : Server {
    private var port = 8088
    private val bootstrap = HttpBootstrap()
    private val services: MutableMap<String, RpcObserver> = HashMap()
    fun subscribe(name: String, body: RpcObserver): RpcService {
        services[name] = body
        return this
    }
    override fun stop() {
        bootstrap.shutdown()
        services.clear()
    }

    override fun start(): Server {
        bootstrap.httpHandler(object : HttpServerHandler() {
            @Throws(IOException::class)
            override fun handle(request: HttpRequest, response: HttpResponse) {
                val service = services[
                    request.getHeader("Service-Name")
                ]
                if (service == null) {
                    response.setHttpStatus(HttpStatus.BAD_REQUEST)
                    response.outputStream.write(0)
                    return
                }
                response.write(
                    FURY.serialize(
                        service.run(
                            FURY.deserialize(
                                request.inputStream.readAllBytes()
                            )
                        )
                    )
                )
            }
        }).setPort(port).start()
        return this
    }

    fun setPort(port: Int): RpcService {
        this.port = port
        return this
    }

    internal val FURY = ThreadLocalFury {
        val ret = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .withClassLoader(it)
            .withAsyncCompilation(true)
            .withCodegen(true)
            .requireClassRegistration(false)
            .withJdkClassSerializableCheck(false)
            .build()
        ret.register(Context::class.java)
        ret
    }
}