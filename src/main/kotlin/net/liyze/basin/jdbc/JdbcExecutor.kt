@file: JvmName("JdbcExecutor")

package net.liyze.basin.jdbc

import java.sql.Connection

//TODO: 2023.9.8
infix fun Connection.select(str: String): Connection {
    return this
}

infix fun Connection.from(str: String): Connection {
    return this
}

infix fun Connection.where(str: String): Connection {
    return this
}

infix fun Connection.orderBy(str: String): Connection {
    return this
}