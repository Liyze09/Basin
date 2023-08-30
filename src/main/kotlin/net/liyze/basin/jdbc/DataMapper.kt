@file:Suppress("unused")

package net.liyze.basin.jdbc

import net.liyze.basin.util.createInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Field
import java.time.*
import java.util.function.Function

class DataMapper<T>(val name: String, val pool: JdbcPool, val clazz: Class<T>) {
    val fields: Array<Field> = clazz.fields

    companion object {
        val logger: Logger = LoggerFactory.getLogger(DataMapper::class.java)
        val converters: MutableMap<Class<*>, Function<String, Any>> = HashMap()

        init {
            converters[LocalDate::class.java] = Function<String, Any> { text: String? -> LocalDate.parse(text) }
            converters[LocalTime::class.java] = Function<String, Any> { text: String? -> LocalTime.parse(text) }
            converters[LocalDateTime::class.java] = Function<String, Any> { text: String? -> LocalDateTime.parse(text) }
            converters[ZonedDateTime::class.java] = Function<String, Any> { text: String? -> ZonedDateTime.parse(text) }
            converters[Duration::class.java] = Function<String, Any> { text: String? -> Duration.parse(text) }
            converters[ZoneId::class.java] = Function<String, Any> { zoneId: String? -> ZoneId.of(zoneId) }
        }
    }

    fun put(obj: T): Long {
        val sql = StringBuilder()
        val args = ArrayList<Any>()
        sql.append("INSERT INTO ")
            .append(name)
            .append(" (")
        fields.forEach {
            sql.append(it.name)
                .append(",")
        }
        sql.deleteCharAt(sql.length - 1)
            .append(") VALUES (")
        fields.forEach {
            if (Number::class.java.isAssignableFrom(it.type)) {
                args.add(it.get(obj) ?: 0)
            } else {
                args.add(it.get(obj)?.toString() ?: "null")
            }
            sql.append("?,")
        }
        sql.deleteCharAt(sql.length - 1)
            .append(")")
        return pool.update(sql.toString(), args)
    }

    fun get(index: Long): T {
        val instance = clazz.createInstance()
        val result = pool.query("SELECT * FROM $name WHERE id = ?", listOf(index))
        for (field in fields) {
            if (converters.containsKey(field.type)) {
                field.set(instance, converters[field.type]?.apply(result.getString(field.name)))
            }
            field.set(instance, result.getObject(field.name, field.type))
        }
        return instance
    }
}