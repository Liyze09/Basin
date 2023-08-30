package net.liyze.basin.rpc

import io.fury.Fury
import io.fury.Language
import net.liyze.basin.util.printException
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.annotations.Contract
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class HttpRpcClient(var url: String) {
    private val results: MutableMap<Int, Optional<Any>> = ConcurrentHashMap()

    fun request(className: String, methodName: String, args: Array<Any?>): Int {
        LOGGER.debug("Request {}.{}::{}", url, className, methodName)
        val sign = random.nextInt()
        val body: RequestBody = FURY.serialize(args).toRequestBody()
        val request: Request = Request.Builder()
            .url(url)
            .addHeader("Class-Name", className)
            .addHeader("Method-Name", methodName)
            .addHeader("Request-Sign", sign.toString())
            .post(body)
            .build()
        results[sign] = Optional.empty()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                LOGGER.printException(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.body != null) {
                    results[Objects.requireNonNull(response.header("Request-Sign"))!!.toInt()] =
                        Optional.of(FURY.deserialize(response.body!!.bytes()))
                }
            }
        })
        return sign
    }

    @Suppress("unused")
    fun getOptionalResult(sign: Int): Optional<Any> {
        return results[sign]!!
    }

    fun getResult(sign: Int): Any {
        while (results[sign]!!.isEmpty) Thread.onSpinWait()
        return results[sign]!!.get()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null || other.javaClass != this.javaClass) return false
        val that = other as HttpRpcClient
        return url == that.url
    }

    override fun hashCode(): Int {
        return Objects.hash(url)
    }

    @Contract(pure = true)
    override fun toString(): String {
        return "HttpRpcClient[$url]"
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(HttpRpcClient::class.java)
        private val random = Random()
        private val client = OkHttpClient()
        private val FURY = Fury.builder()
            .withLanguage(Language.JAVA)
            .withRefTracking(true)
            .requireClassRegistration(false)
            .buildThreadSafeFuryPool(1, 8, 16, TimeUnit.SECONDS)
    }
}
