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
package net.liyze.basin.common.resource

import java.util.function.Supplier

class ThreadLocalBean<T>(
    val singleton: Supplier<T>,
    override val type: Class<in T>
) : AbstractBean<T>() {
    @Suppress("UNCHECKED_CAST")
    constructor(singleton: T, type: Class<in T> = singleton!!::class.java as Class<T>) : this(
        Supplier { singleton },
        type
    )

    val threadLocal: ThreadLocal<T> = ThreadLocal.withInitial { return@withInitial singleton.get() }
    override fun getInstance(): T {
        return threadLocal.get()
    }
}