package net.liyze.basin.rpc

import io.fury.Fury
import io.fury.Language
import net.liyze.basin.core.Server
import net.liyze.basin.rpc.annotation.RpcService
import net.liyze.basin.util.getBean
import net.liyze.basin.util.printException
import org.slf4j.LoggerFactory
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.IOException
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.TimeUnit

class RpcServer(val port: Int) : Server {
    private val bootstrap = HttpBootstrap()
    override fun stop() {
        bootstrap.shutdown()
    }

    override fun start(): Server {
        bootstrap.configuration().serverName("rpc")
        bootstrap.httpHandler(object : HttpServerHandler() {
            @Throws(IOException::class)
            override fun handle(request: HttpRequest, response: HttpResponse) {
                val className = request.getHeader("Class-Name")
                val methodName = request.getHeader("Method-Name")
                val stream: OutputStream = response.outputStream
                val args = FURY.deserialize(request.inputStream.readAllBytes()) as Array<*>
                val bean = getBean(className)
                response.addHeader("Request-Sign", request.getHeader("Request-Sign"))
                if (bean != null) {
                    try {
                        val ret = invoke(bean.beanClass, methodName, arrayOf(args), bean.instance)
                        FURY.currentFury.register(ret.javaClass)
                        stream.write(FURY.serialize(ret))
                    } catch (e: IllegalArgumentException) {
                        LOGGER.printException(e)
                        response.setHttpStatus(HttpStatus.BAD_REQUEST)
                    } catch (e: IllegalAccessException) {
                        LOGGER.printException(e)
                        response.setHttpStatus(HttpStatus.BAD_REQUEST)
                    } catch (e: InvocationTargetException) {
                        LOGGER.printException(e)
                        response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                } else {
                    response.setHttpStatus(HttpStatus.BAD_REQUEST)
                }
                stream.flush()
            }
        })
        bootstrap.setPort(port).start()
        return this
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RpcServer::class.java)
        private val FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .withAsyncCompilation(true)
            .buildThreadSafeFuryPool(2, 32, 8, TimeUnit.SECONDS)

        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        private operator fun invoke(clazz: Class<*>, name: String, args: Array<Any>, `object`: Any?): Any {
            for (method in clazz.getMethods()) {
                if (method.parameterCount == args.size && method.name == name && method.isAnnotationPresent(RpcService::class.java)) {
                    return method.invoke(`object`, *args)
                }
            }
            throw IllegalArgumentException()
        }
    }
}
