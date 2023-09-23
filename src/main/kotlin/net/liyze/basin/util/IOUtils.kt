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
@file: JvmName("IOUtils")

package net.liyze.basin.util

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
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

fun InputStream.toDataInput(): DataInputStream {
    return DataInputStream(this)
}

fun OutputStream.toDataOutput(): DataOutputStream {
    return DataOutputStream(this)
}

@JvmOverloads
fun ByteArray.getString(charset: Charset = StandardCharsets.UTF_8): String {
    return String(this, charset)
}
