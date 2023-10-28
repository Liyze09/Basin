/*
 * Copyright (c) 2023 Liyze09
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package net.liyze.basin.common.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.liyze.basin.common.async.Callable
import net.liyze.basin.common.async.Result
import net.liyze.basin.common.toList
import java.io.Closeable
import java.sql.Connection
import java.sql.PreparedStatement
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
    fun query(sql: String, args: List<Any> = listOf()): JdbcResult<List<Map<String, Any>>> {
        return execute { connection ->
            var ps: PreparedStatement? = null
            try {
                ps = connection.prepareStatement(sql)
                for ((index, value) in args.withIndex()) {
                    ps.setObject(index + 1, value)
                }
                val ret = ps.executeQuery()
                connection.commit()
                return@execute ret.toList()
            } catch (e: SQLException) {
                connection.rollback()
                throw RuntimeException(e)
            } finally {
                ps?.close()
                connection.close()
            }
        }
    }

    @JvmOverloads
    fun update(sql: String, args: List<Any> = listOf()): Long {
        val connection = getConnection()
        var ps: PreparedStatement? = null
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
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
            ps?.close()
            connection.close()
        }
    }

    @JvmOverloads
    fun asyncUpdate(sql: String, args: List<Any> = listOf()): JdbcResult<Long> {
        return execute { connection ->
            var ps: PreparedStatement? = null
            try {
                ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                for ((index, value) in args.withIndex()) {
                    ps.setObject(index + 1, value)
                }
                ps.executeUpdate()
                connection.commit()
                return@execute ps.generatedKeys.use { rs ->
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
                ps?.close()
                connection.close()
            }
        }
    }

    infix fun <T> execute(action: Callable<Connection, T>): JdbcResult<T> {
        val connection = getConnection()
        return JdbcResult(action, connection)
    }

    fun executeBatch(sql: String, vararg args: List<Any>) {
        val connection = getConnection()
        var ps: PreparedStatement? = null
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            for (list in args) {
                for ((index, value) in list.withIndex()) {
                    ps.setObject(index + 1, value)
                }
                ps.addBatch()
            }
            ps.executeBatch()
            connection.commit()
        } catch (e: SQLException) {
            connection.rollback()
            throw RuntimeException(e)
        } finally {
            ps?.close()
            connection.close()
        }
    }

    override fun close() = data?.close() ?: throw IllegalStateException("Must connect SQL before close!")

    class JdbcResult<T>(
        action: Callable<Connection, T>,
        val connection: Connection
    ) : Result<Connection, T>(action, connection) {
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