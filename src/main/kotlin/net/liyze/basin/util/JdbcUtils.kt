@file: JvmName("JdbcUtils")

package net.liyze.basin.util

import java.sql.ResultSet

fun ResultSet.toList(): List<Map<String, Any>> {
    val ret: MutableList<Map<String, Any>> = ArrayList()
    val meta = this.metaData
    val column = meta.columnCount
    while (this.next()) {
        val data: MutableMap<String, Any> = HashMap(column)
        for (i in 1..column) {
            data[meta.getColumnName(i)] = this.getObject(i)
        }
        ret.add(data)
    }
    return ret
}