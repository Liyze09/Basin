package net.liyze.basin.http

import com.google.gson.Gson
import net.liyze.basin.context.ApplicationContext
import net.liyze.basin.core.Server
import net.liyze.basin.core.contexts
import net.liyze.basin.http.annotation.Control
import net.liyze.basin.http.annotation.GetMapping
import net.liyze.basin.http.annotation.PostMapping
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
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer

class HttpServer(val serverName: String, val port: Int) : Server {
    val cfg: Configuration = Configuration.defaultConfiguration()
    private val cgt: GroupTemplate
    private val fgt: GroupTemplate
    private val bootstrap = HttpBootstrap()
    val root: File = File("data/web/$serverName/root".replace('/', File.separatorChar))
    var getMappings: MutableMap<String, GetDispatcher> = HashMap()
    var postMappings: MutableMap<String, PostDispatcher> = HashMap()
    var gson = Gson()

    init {
        root.mkdirs()
        val temp = Path.of(root.path).resolve("template").toFile()
        temp.mkdirs()
        contexts.forEach(
            Consumer { context: ApplicationContext ->
                context.getBeans(
                    WebController::class.java
                )
                    ?.forEach(Consumer { bean: WebController ->
                        val annotation0 = bean.javaClass.getAnnotation(
                            Control::class.java
                        )
                        if (annotation0 != null && annotation0.serverName == serverName) {
                            Arrays.stream(bean.javaClass.getMethods())
                                .forEach { method: Method ->
                                    val annotation = method.getAnnotation(
                                        GetMapping::class.java
                                    )
                                    if (annotation != null) {
                                        getMappings[annotation.path] =
                                            GetDispatcher(bean, method, method.parameterTypes)
                                    }
                                    val annotation1 = method.getAnnotation(PostMapping::class.java)
                                    if (annotation1 != null) {
                                        postMappings[annotation1.path] =
                                            PostDispatcher(bean, method, method.parameterTypes, gson)
                                    }
                                }
                        }
                    })
            }
        )
        cfg.isDirectByteOutput = true
        val fileLoader: ResourceLoader<String> = FileResourceLoader(
            "data" + File.separator + "web" + File.separator + serverName + "template"
        )
        val classpathLoader: ResourceLoader<String> = ClasspathResourceLoader("static/" + serverName + "template")
        cgt = GroupTemplate(classpathLoader, cfg)
        fgt = GroupTemplate(fileLoader, cfg)
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

    @Throws(InvocationTargetException::class, IllegalAccessException::class, IOException::class)
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
        val view = dispatcher.invoke(request, response)
        if (view != null) {
            if (view.view.startsWith("redirect:")) {
                response.setHeader("Location", view.view.substring(9).trim())
                if (response.httpStatus == 200) {
                    response.setHttpStatus(HttpStatus.TEMPORARY_REDIRECT)
                }
            }
            render(view, response)
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

    @Throws(InvocationTargetException::class, IllegalAccessException::class, IOException::class)
    private fun postDispatch(request: HttpRequest, response: HttpResponse) {
        val dispatcher = getPostDispatcher(request.requestURI)
        if (dispatcher == null) {
            response.setHttpStatus(HttpStatus.NOT_FOUND)
            getFileResource("/404.html", response)
            return
        }
        val view = dispatcher.invoke(request, response)
        if (view != null) {
            if (view.view.startsWith("redirect:")) {
                response.setHeader("Location", view.view.substring(9).trim())
                if (response.httpStatus == 200) {
                    response.setHttpStatus(HttpStatus.TEMPORARY_REDIRECT)
                }
            }
            render(view, response)
        }
    }

    private fun getGetDispatcher(uri: String): GetDispatcher? {
        var result = getMappings[uri]
        if (!uri.contains("/")) return null
        if (result == null) {
            result = getGetDispatcher(uri.substring(0, uri.lastIndexOf("/")))
        }
        return result
    }

    private fun getPostDispatcher(uri: String): PostDispatcher? {
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
