package net.liyze.basin.http

import net.liyze.basin.core.Server
import org.beetl.core.Configuration
import org.beetl.core.GroupTemplate
import org.beetl.core.ResourceLoader
import org.beetl.core.Template
import org.beetl.core.resource.ClasspathResourceLoader
import org.beetl.core.resource.FileResourceLoader
import org.smartboot.http.common.enums.HttpStatus
import org.smartboot.http.server.HttpBootstrap
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import org.smartboot.http.server.HttpServerHandler
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.file.Path
import java.util.*

object HttpServer : Server {
    var serverName = "Basin.HTTP"
    var port = 8000
    val cfg: Configuration = Configuration.defaultConfiguration()
    private val cgt: GroupTemplate
    private val fgt: GroupTemplate
    private val bootstrap = HttpBootstrap()
    val root: File = File("data/web/$serverName/root".replace('/', File.separatorChar))
    val getMappings: MutableMap<String, HttpHandler> = HashMap()
    val postMappings: MutableMap<String, HttpHandler> = HashMap()

    init {
        root.mkdirs()
        val temp = Path.of(root.path).resolve("template").toFile()
        temp.mkdirs()
        cfg.isDirectByteOutput = true
        val fileLoader: ResourceLoader<String> = FileResourceLoader(
            "data" + File.separator + "web" + File.separator + serverName + "template"
        )
        val classpathLoader: ResourceLoader<String> = ClasspathResourceLoader("static/" + serverName + "template")
        cgt = GroupTemplate(classpathLoader, cfg)
        fgt = GroupTemplate(fileLoader, cfg)
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
        val dispatcher = getGetDispatcher(request.requestURI)
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
        val template: Template = if (temp.startsWith("classpath:")) {
            cgt.getTemplate(view.view)
        } else if (temp.startsWith("file:")) {
            fgt.getTemplate(view.view)
        } else {
            throw RuntimeException("Illegal view path: " + view.view)
        }
        template.renderTo(response.outputStream)
    }

    private fun postDispatch(request: HttpRequest, response: HttpResponse) {
        val dispatcher = getPostDispatcher(request.requestURI)
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

    private fun getGetDispatcher(uri: String): HttpHandler? {
        var result = getMappings[uri]
        if (!uri.contains("/")) return null
        if (result == null) {
            result = getGetDispatcher(uri.substring(0, uri.lastIndexOf("/")))
        }
        return result
    }

    private fun getPostDispatcher(uri: String): HttpHandler? {
        var result = postMappings[uri]
        if (!uri.contains("/")) return null
        if (result == null) {
            result = getPostDispatcher(uri.substring(0, uri.lastIndexOf("/")))
        }
        return result
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
                    "data" + File.separator + "web" + File.separator
                            + serverName + uri.replace('/', File.separatorChar)
                )
                if (tmp0 != null) {
                    response.write(tmp0.readAllBytes())
                } else if (tmp2.exists()) {
                    FileInputStream(tmp2).use { input -> response.write(input.readAllBytes()) }
                } else if (tmp1 != null) {
                    response.write(tmp1.readAllBytes())
                } else {
                    response.setHttpStatus(HttpStatus.NOT_FOUND)
                    getFileResource("/404.html", response)
                }
            }
        }
    }
}