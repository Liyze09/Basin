package net.liyze.basin.context.io

import net.liyze.basin.core.publicVars
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.*
import java.util.*
import java.util.function.Function

class PropertyResolver {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(PropertyResolver::class.java)
        val properties: MutableMap<String, String> = HashMap()
        val converters: MutableMap<Class<*>?, Function<String, Any>> = HashMap()

        init {
            properties.putAll(publicVars)
            if (LOGGER.isDebugEnabled) {
                val keys: MutableList<String> = ArrayList(properties.keys)
                keys.sort()
                for (key in keys) {
                    LOGGER.debug("PropertyResolver: {} = {}", key, properties[key])
                }
            }
            // register converters:
            converters[String::class.java] = Function { s: String -> s }
            converters[Boolean::class.javaPrimitiveType] = Function { s: String -> s.toBoolean() }
            converters[Boolean::class.java] = Function { s: String -> s }
            converters[Byte::class.javaPrimitiveType] = Function { s: String -> s.toByte() }
            converters[Byte::class.java] = Function { s: String -> s }
            converters[Short::class.javaPrimitiveType] = Function { s: String -> s.toShort() }
            converters[Short::class.java] = Function { s: String -> s }
            converters[Int::class.javaPrimitiveType] = Function { s: String -> s.toInt() }
            converters[Int::class.java] = Function { s: String -> s }
            converters[Long::class.javaPrimitiveType] = Function { s: String -> s.toLong() }
            converters[Long::class.java] = Function { s: String -> s }
            converters[Float::class.javaPrimitiveType] = Function { s: String -> s.toFloat() }
            converters[Float::class.java] = Function { s: String -> s }
            converters[Double::class.javaPrimitiveType] = Function { s: String -> s.toDouble() }
            converters[Double::class.java] = Function { s: String -> s }
            converters[LocalDate::class.java] = Function { text: String? -> LocalDate.parse(text) }
            converters[LocalTime::class.java] = Function { text: String? -> LocalTime.parse(text) }
            converters[LocalDateTime::class.java] = Function { text: String? -> LocalDateTime.parse(text) }
            converters[ZonedDateTime::class.java] = Function { text: String? -> ZonedDateTime.parse(text) }
            converters[Duration::class.java] = Function { text: String? -> Duration.parse(text) }
            converters[ZoneId::class.java] = Function { zoneId: String? -> ZoneId.of(zoneId) }
        }
    }

    fun containsProperty(key: String): Boolean {
        return properties.containsKey(key)
    }

    fun getProperty(key: String): String? {
        val keyExpr = parsePropertyExpr(key)
        if (keyExpr != null) {
            return if (keyExpr.defaultValue != null) {
                getProperty(keyExpr.key, keyExpr.defaultValue)
            } else {
                getRequiredProperty(keyExpr.key)
            }
        }
        val value = properties[key]
        return value?.let { parseValue(it) }
    }

    fun getProperty(key: String, defaultValue: String?): String? {
        val value = getProperty(key)
        return value ?: parseValue(defaultValue)
    }

    fun <T> getProperty(key: String, targetType: Class<T>): T? {
        val value = getProperty(key) ?: return null
        return convert(targetType, value)
    }

    fun <T> getProperty(key: String, targetType: Class<T>, defaultValue: T): T {
        val value = getProperty(key) ?: return defaultValue
        return convert(targetType, value)
    }

    fun getRequiredProperty(key: String): String {
        val value = getProperty(key)
        return Objects.requireNonNull(value, "Property '$key' not found.")!!
    }

    fun <T> getRequiredProperty(key: String, targetType: Class<T>): T {
        val value: T? = getProperty(key, targetType)
        return Objects.requireNonNull(value, "Property '$key' not found.")!!
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> convert(clazz: Class<*>, value: String): T {
        val fn = converters[clazz]
            ?: throw IllegalArgumentException("Unsupported value type: " + clazz.getName())
        return fn.apply(value) as T
    }

    fun parseValue(value: String?): String? {
        val (key, defaultValue) = parsePropertyExpr(value) ?: return value
        return defaultValue?.let { getProperty(key, it) } ?: getRequiredProperty(key)
    }

    fun parsePropertyExpr(key: String?): PropertyExpr? {
        if (key!!.startsWith("\${") && key.endsWith("}")) {
            val n = key.indexOf(':')
            return if (n == -1) {
                // no default value: ${key}
                val k = notEmpty(key.substring(2, key.length - 1))
                PropertyExpr(k, null)
            } else {
                // has default value: ${key:default}
                val k = notEmpty(key.substring(2, n))
                PropertyExpr(k, key.substring(n + 1, key.length - 1))
            }
        }
        return null
    }

    fun notEmpty(key: String): String {
        require(key.isNotEmpty()) { "Invalid key: $key" }
        return key
    }
}

@JvmRecord
data class PropertyExpr(val key: String, val defaultValue: String?)
