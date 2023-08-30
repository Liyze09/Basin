package net.liyze.basin.context.io

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UncheckedIOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function

/**
 * A simple classpath scan works both in directory and jar:
 *
 *
 * [...](https://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection#58773038)
 */
@Suppress("NAME_SHADOWING")
class ResourceResolver(var basePackage: String) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(ResourceResolver::class.java)
    }

    fun <R> scan(mapper: Function<Resource?, R>): List<R> {
        val basePackagePath = basePackage.replace(".", "/")
        return try {
            val collector: MutableList<R> = ArrayList()
            scan0(basePackagePath, basePackagePath, collector, mapper)
            collector
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class, URISyntaxException::class)
    fun <R> scan0(basePackagePath: String, path: String?, collector: MutableList<R>, mapper: Function<Resource?, R>) {
        logger.atDebug().log("scan path: {}", path)
        val en = contextClassLoader!!.getResources(path)
        while (en.hasMoreElements()) {
            val url = en.nextElement()
            val uri = url.toURI()
            val uriStr = removeTrailingSlash(URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8))
            var uriBaseStr = uriStr.substring(0, uriStr.length - basePackagePath.length)
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring(5)
            }
            if (uriStr.startsWith("jar:")) {
                scanFile(true, uriBaseStr, jarUriToPath(basePackagePath, uri), collector, mapper)
            } else {
                scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper)
            }
        }
    }

    val contextClassLoader: ClassLoader?
        get() {
            var cl: ClassLoader?
            cl = Thread.currentThread().getContextClassLoader()
            if (cl == null) {
                cl = javaClass.getClassLoader()
            }
            return cl
        }

    fun jarUriToPath(basePackagePath: String, jarUri: URI?): Path {
        val path: Path = try {
            FileSystems.newFileSystem(jarUri, HashMap<String, Any>()).getPath(basePackagePath)
        } catch (e: Exception) {
            FileSystems.getFileSystem(jarUri).getPath(basePackagePath)
        }
        return path
    }

    @Throws(IOException::class)
    fun <R> scanFile(
        isJar: Boolean,
        base: String,
        root: Path,
        collector: MutableList<R>,
        mapper: Function<Resource?, R>
    ) {
        val baseDir = removeTrailingSlash(base)
        Files.walk(root).filter { path: Path -> Files.isRegularFile(path) }
            .forEach { file: Path ->
                val res: Resource = if (isJar) {
                    Resource(baseDir, removeLeadingSlash(file.toString()))
                } else {
                    val path = file.toString()
                    val name = removeLeadingSlash(path.substring(baseDir.length))
                    Resource("file:$path", name)
                }
                logger.atDebug().log("found resource: {}", res)
                val r: R? = mapper.apply(res)
                if (r != null) {
                    collector.add(r)
                }
            }
    }

    fun removeLeadingSlash(s: String): String {
        var s = s
        if (s.startsWith("/") || s.startsWith("\\")) {
            s = s.substring(1)
        }
        return s
    }

    fun removeTrailingSlash(s: String): String {
        var s = s
        if (s.endsWith("/") || s.endsWith("\\")) {
            s = s.substring(0, s.length - 1)
        }
        return s
    }
}
