package net.liyze.basin.rpc

import io.fury.Fury
import io.fury.Language
import io.fury.ThreadSafeFury
import net.liyze.basin.core.Server
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.IOException

object RpcService : Server {
    private var port = 8088
    private val bootstrap = HttpBootstrap()
    private val services: MutableMap<String, Service> = HashMap()
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
                        service.body(
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

    val FURY: ThreadSafeFury = Fury
        .builder()
        .withAsyncCompilation(true)
        .withCodegen(true)
        .withRefTracking(true)
        .withLanguage(Language.JAVA)
        .requireClassRegistration(false)
        .buildThreadSafeFury()
}