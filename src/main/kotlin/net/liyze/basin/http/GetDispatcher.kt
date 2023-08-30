package net.liyze.basin.http

import org.jetbrains.annotations.Contract
import org.smartboot.http.server.HttpRequest
import org.smartboot.http.server.HttpResponse
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@JvmRecord
data class GetDispatcher(val instance: Any, val method: Method, val argTypes: Array<Class<*>>) {
    @Contract(pure = true)
    @Throws(InvocationTargetException::class, IllegalAccessException::class)
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

                String::class.java -> {
                    args[i] = request.requestURI
                }

                else -> {
                    throw RuntimeException("Missing handler for type: $type")
                }
            }
        }
        return method.invoke(instance, *args) as ModelAndView?
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GetDispatcher

        if (instance != other.instance) return false
        if (method != other.method) return false
        if (!argTypes.contentEquals(other.argTypes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = instance.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + argTypes.contentHashCode()
        return result
    }
}
