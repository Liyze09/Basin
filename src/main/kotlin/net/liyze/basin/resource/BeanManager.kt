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

package net.liyze.basin.resource

import net.liyze.basin.resource.exception.BeanNotFoundException

@Suppress("UNCHECKED_CAST")
object BeanManager {
    val beans: MutableMap<Class<*>, AbstractBean<*>> = HashMap()
    fun <T> getBean(type: Class<T>): T {
        val bean = beans[type] ?: throw BeanNotFoundException(type.name)
        return bean.getInstance() as T
    }

    fun <T> findBean(type: Class<T>): T? {
        val bean = beans[type] ?: return null
        return bean.getInstance() as T
    }

    fun addBean(bean: AbstractBean<*>) {
        beans[bean.type] = bean
    }

    fun close() {
        beans.forEach {
            it.value.destroy()
        }
        beans.clear()
    }
}