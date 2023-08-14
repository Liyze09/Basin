@file:Suppress("unused")

package net.liyze.basin.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

class JdbcPool {
    private val config: HikariConfig = HikariConfig()

    @Volatile
    private var data: HikariDataSource? = null
    fun connect(jdbcUrl: String, userName: String, password: String): JdbcPool {
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
    fun query(sql: String, args: List<Any>): ResultSet? {
        val connection = getConnection()
        try {
            val ps = connection.prepareStatement(sql)
            for ((index, value) in args.withIndex()) {
                ps.setObject(index, value)
            }
            val ret = ps.executeQuery()
            connection.commit()
            return ret
        } catch (_: SQLException) {
            connection.rollback()
        } finally {
            connection.close()
        }
        return null
    }

    fun update(sql: String, args: List<Any>): ResultSet? {
        val connection = getConnection()
        try {
            val ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            for ((index, value) in args.withIndex()) {
                ps.setObject(index, value)
            }
            ps.executeUpdate()
            connection.commit()
            return ps.generatedKeys
        } catch (_: SQLException) {
            connection.rollback()
        } finally {
            connection.close()
        }
        return null
    }

    fun <T> execute(action: Callback<T>): JdbcResult<T> {
        val connection = getConnection()
        return JdbcResult(action, connection)
    }

    fun close() = data?.close() ?: throw RuntimeException("Must connect SQL before use!")

    @FunctionalInterface
    interface Callback<T> {
        fun run(connection: Connection): T
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
                    val ret: T = action.run(connection)
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