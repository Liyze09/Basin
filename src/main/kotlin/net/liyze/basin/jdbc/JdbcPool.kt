@file:Suppress("unused")

package net.liyze.basin.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.liyze.basin.util.toList
import java.io.Closeable
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement

class JdbcPool : Closeable {
    private val config: HikariConfig = HikariConfig()

    @Volatile
    private var data: HikariDataSource? = null

    @JvmOverloads
    fun connect(
        jdbcUrl: String,
        userName: String,
        password: String = ""
    ): JdbcPool {
        config.username = userName
        config.jdbcUrl = jdbcUrl
        config.password = password
        config.setAutoCommit(false)
        config.maximumPoolSize = 16
        config.minimumIdle = 4
        config.maxLifetime = 60000
        config.idleTimeout = 0
        data = HikariDataSource(config)
        return this
    }

    fun getConfig() = config
    fun getConnection() = data?.getConnection() ?: throw IllegalStateException("Must connect SQL before use!")
    @JvmOverloads
    fun query(sql: String, args: List<Any> = listOf()): List<Map<String, Any>> {
        val connection = getConnection()
        try {
            val ps = connection.prepareStatement(sql)
            for ((index, value) in args.withIndex()) {
                ps.setObject(index + 1, value)
            }
            val ret = ps.executeQuery()
            connection.commit()
            return ret.toList()
        } catch (e: SQLException) {
            connection.rollback()
            throw RuntimeException(e)
        } finally {
            connection.close()
        }
    }

    @JvmOverloads
    fun update(sql: String, args: List<Any> = listOf()): Long {
        val connection = getConnection()
        try {
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            for ((index, value) in args.withIndex()) {
                ps.setObject(index + 1, value)
            }
            ps.executeUpdate()
            connection.commit()
            return ps.generatedKeys.use { rs ->
                var id: Long = -1
                if (rs.next()) {
                    id = rs.getLong(1)
                }
                return@use id
            }
        } catch (e: SQLException) {
            connection.rollback()
            throw RuntimeException(e)
        } finally {
            connection.close()
        }
    }

    infix fun <T> execute(action: net.liyze.basin.async.Callable<Connection, T>): JdbcResult<T> {
        val connection = getConnection()
        return JdbcResult(action, connection)
    }

    override fun close() = data?.close() ?: throw IllegalStateException("Must connect SQL before use!")

    class JdbcResult<T>(
        action: net.liyze.basin.async.Callable<Connection, T>,
        val connection: Connection
    ) : net.liyze.basin.async.Result<Connection, T>(action, connection) {
        override fun run(): T {
            try {
                val ret: T = action run connection
                connection.commit()
                return ret
            } catch (e: Throwable) {
                connection.rollback()
                throw RuntimeException(e)
            }
        }
    }
}