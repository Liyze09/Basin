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
@file: JvmName("ClassUtils")

package net.liyze.basin.util

import net.liyze.basin.rpc.RpcService.FURY
import org.slf4j.LoggerFactory
import sun.misc.Unsafe

private val LOGGER = LoggerFactory.getLogger("ClassUtils")
fun <T> Class<T>.createInstance(): T {
    var instance: T
    try {
        instance = getDeclaredConstructor().newInstance()
    } catch (_: Exception) {
        LOGGER.warn("Using Unsafe to create instance!")
        val field = Unsafe::class.java.getDeclaredField("theUnsafe")
        field.setAccessible(true)
        @Suppress("UNCHECKED_CAST")
        instance = (field.get(null) as Unsafe).allocateInstance(this) as T
    }
    return instance
}

fun Any.toBytes(): ByteArray = FURY.serialize(this)
fun ByteArray.toObject(): Any = FURY.deserialize(this)

@Suppress("UNCHECKED_CAST")
fun <T> T.deepClone(): T = this?.toBytes()?.toObject() as T