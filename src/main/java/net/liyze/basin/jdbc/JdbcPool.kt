@file:Suppress("unused")

package net.liyze.basin.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.sql.Connection
import java.sql.ResultSet
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
    fun getConnection() = data?.getConnection() ?: throw RuntimeException("Must connect SQL before use!")
    @JvmOverloads
    fun query(sql: String, args: List<Any> = listOf()): ResultSet {
        val connection = getConnection()
        try {
            val ps = connection.prepareStatement(sql)
            for ((index, value) in args.withIndex()) {
                ps.setObject(index + 1, value)
            }
            val ret = ps.executeQuery()
            connection.commit()
            return ret
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

    infix fun <T> execute(action: Callback<T>): JdbcResult<T> {
        val connection = getConnection()
        return JdbcResult(action, connection)
    }

    override fun close() = data?.close() ?: throw RuntimeException("Must connect SQL before use!")

    @FunctionalInterface
    fun interface Callback<T> {
        infix fun run(connection: Connection): T
    }

    @OptIn(DelicateCoroutinesApi::class)
    class JdbcResult<T>(
        val action: Callback<T>,
        val connection: Connection,
    ) {
        @Volatile
        var result: T? = null

        init {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val ret: T = action run connection
                    connection.commit()
                    result = ret
                } catch (e: Throwable) {
                    connection.rollback()
                    throw RuntimeException(e)
                }
            }
        }

        fun get(): T {
            while (result == null) {
                Thread.onSpinWait()
            }
            return result!!
        }
    }
}