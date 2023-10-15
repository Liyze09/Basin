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

package net.liyze.basin.http

import net.liyze.basin.core.LOGGER
import net.liyze.basin.core.Server
import net.liyze.basin.util.read
import org.beetl.core.Configuration
import org.beetl.core.GroupTemplate
import org.beetl.core.ResourceLoader
import org.beetl.core.Template
import org.beetl.core.resource.ClasspathResourceLoader
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.*
import java.util.*

object HttpServer : Server {
    var serverName = "Basin.HTTP"
    var port = 8000
    val cfg: Configuration = Configuration.defaultConfiguration()
    private val cgt: GroupTemplate
    private val bootstrap = HttpBootstrap()
    val getMappings: MutableMap<String, HttpHandler> = HashMap()
    val postMappings: MutableMap<String, HttpHandler> = HashMap()

    init {
        val classpathLoader: ResourceLoader<String> = ClasspathResourceLoader("static/template")
        cgt = GroupTemplate(classpathLoader, cfg)
    }

    fun subscribeGet(path: String, handler: HttpHandler) {
        getMappings[path] = handler
    }

    fun subscribePost(path: String, handler: HttpHandler) {
        postMappings[path] = handler
    }

    override fun stop() {
        bootstrap.shutdown()
    }

    override fun start(): Server {
        LOGGER.info("Basin.HTTP starting")
        bootstrap.configuration().serverName(serverName)
        bootstrap.httpHandler(object : HttpServerHandler() {
            @Throws(IOException::class)
            override fun handle(request: HttpRequest, response: HttpResponse) {
                try {
                    if (request.method.equals("get", ignoreCase = true)) {
                        getDispatch(request, response)
                    } else if (request.method.equals("post", ignoreCase = true)) {
                        postDispatch(request, response)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    getFileResource("/500.html", response)
                }
            }
        }).setPort(port).start()
        return this
    }

    private fun getDispatch(request: HttpRequest, response: HttpResponse) {
        if (request.requestURI.startsWith("/favicon.ico") || request.requestURI.startsWith("/static")) {
            staticResource(request, response)
            return
        }
        val dispatcher = getMappings[request.requestURI]
        if (dispatcher == null) {
            staticResource(request, response)
            return
        }
        val view = dispatcher.handle(HttpJob(request, response))
        if (view.model !is Processed) {
            if (view.view.startsWith("redirect:")) {
                response.setHeader("Location", view.view.substring(9).trim())
                if (response.httpStatus == 200) {
                    response.setHttpStatus(HttpStatus.TEMPORARY_REDIRECT)
                }
            }
            when (view.model) {
                is ByteArray -> {
                    response.write(view.model)
                }

                is String -> {
                    response.write(view.model.encodeToByteArray())
                }

                else -> render(view, response)
            }
        }
    }

    private fun render(view: ModelAndView, response: HttpResponse) {
        val temp = view.view
        val template: Template = if (temp.startsWith("/")) {
            cgt.getTemplate(view.view)
        } else {
            throw RuntimeException("Illegal view path: " + view.view)
        }
        template.renderTo(response.outputStream)
    }

    private fun postDispatch(request: HttpRequest, response: HttpResponse) {
        val dispatcher = postMappings[request.requestURI]
        if (dispatcher == null) {
            response.setHttpStatus(HttpStatus.NOT_FOUND)
            getFileResource("/404.html", response)
            return
        }
        val view = dispatcher.handle(HttpJob(request, response))
        if (view.model !is Processed) {
            if (view.view.startsWith("redirect:")) {
                response.setHeader("Location", view.view.substring(9).trim())
                if (response.httpStatus == 200) {
                    response.setHttpStatus(HttpStatus.TEMPORARY_REDIRECT)
                }
            }
            when (view.model) {
                is ByteArray -> {
                    response.write(view.model)
                }

                is String -> {
                    response.write(view.model.encodeToByteArray())
                }

                else -> render(view, response)
            }
        }
    }

    @Throws(IOException::class)
    private fun staticResource(request: HttpRequest, response: HttpResponse) {
        getFileResource(request.requestURI, response)
    }

    @Suppress("NAME_SHADOWING")
    @Throws(IOException::class)
    private fun getFileResource(uri: String, response: HttpResponse) {
        var uri = uri
        if (uri == "/" || uri.isBlank()) uri = "/index.html"
        HttpServer::class.java.getResourceAsStream("/static/$serverName$uri").use { tmp0 ->
            HttpServer::class.java.getResourceAsStream(
                "/static$uri"
            ).use { tmp1 ->
                val tmp2 = File(
                    "static" + File.separator
                            + serverName + uri.replace('/', File.separatorChar)
                )
                if (tmp0 != null) {
                    read(tmp0, response.outputStream)
                } else if (tmp2.exists()) {
                    FileInputStream(tmp2).use { read(it, response.outputStream) }
                } else if (tmp1 != null) {
                    read(tmp1, response.outputStream)
                } else {
                    response.setHttpStatus(HttpStatus.NOT_FOUND)
                    getFileResource("/404.html", response)
                }
            }
        }
    }
}