package net.liyze.basin.http

import com.google.gson.Gson
import org.jetbrains.annotations.Contract
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@JvmRecord
data class PostDispatcher(val instance: Any, val method: Method, val argTypes: Array<Class<*>>, val gson: Gson) {
    @Contract(pure = true)
    @Throws(InvocationTargetException::class, IllegalAccessException::class, IOException::class)
    operator fun invoke(request: HttpRequest, response: HttpResponse?): ModelAndView? {
        val args = arrayOfNulls<Any>(argTypes.size)
        for (i in argTypes.indices) {
            when (val type = argTypes[i]) {
                HttpRequest::class.java -> {
                    args[i] = request
                }

                HttpResponse::class.java -> {
                    args[i] = response
                }

                else -> {
                    val out = ByteArrayOutputStream()
                    val bytes = ByteArray(1024)
                    var len: Int
                    val inputStream = request.inputStream
                    while (inputStream.read(bytes).also { len = it } != -1) {
                        out.write(bytes, 0, len)
                    }
                    args[i] = gson.fromJson(out.toString(request.characterEncoding), type)
                }
            }
        }
        return method.invoke(instance, *args) as ModelAndView?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PostDispatcher

        if (instance != other.instance) return false
        if (method != other.method) return false
        if (!argTypes.contentEquals(other.argTypes)) return false
        if (gson != other.gson) return false

        return true
    }

    override fun hashCode(): Int {
        var result = instance.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + argTypes.contentHashCode()
        result = 31 * result + gson.hashCode()
        return result
    }
}
