package net.liyze.basin.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class JdbcPool {
    private val config: HikariConfig = HikariConfig()
    private var data: HikariDataSource? = null
    private val pool = Executors.newCachedThreadPool()
    fun connect(jdbcUrl: String, userName: String, password: String): JdbcPool {
        config.username = userName
        config.jdbcUrl = jdbcUrl
        config.password = password
        config.setAutoCommit(false)
        config.maximumPoolSize = 16
        config.minimumIdle = 4
        config.maxLifetime = 60000
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

    fun <T> execute(action: Callback<T>): Future<T>? {
        val connection = getConnection()
        return pool.submit(Callable {
            try {
                val ret: T? = action.run(connection)
                connection.commit()
                return@Callable ret
            } catch (e: SQLException) {
                connection.rollback()
                throw RuntimeException(e)
            }
        })
    }

    fun close() = data?.close()

    @FunctionalInterface
    interface Callback<T> {
        fun run(connection: Connection): T?
    }
}