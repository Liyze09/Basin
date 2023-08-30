@file: JvmName("ClassUtils")

package net.liyze.basin.util

import org.slf4j.LoggerFactory
import sun.misc.Unsafe

private val LOGGER = LoggerFactory.getLogger("ClassUtils")
fun <T> Class<T>.createInstance(): T {
    var instance: T
    try {
        instance = getConstructor().newInstance()
    } catch (_: Exception) {
        try {
            instance = getDeclaredConstructor().newInstance()
        } catch (_: Exception) {
            LOGGER.warn("Using Unsafe to create instance!")
            val field = Unsafe::class.java.getDeclaredField("theUnsafe")
            field.setAccessible(true)
            @Suppress("UNCHECKED_CAST")
            instance = (field.get(null) as Unsafe).allocateInstance(this) as T
        }
    }
    return instance
}