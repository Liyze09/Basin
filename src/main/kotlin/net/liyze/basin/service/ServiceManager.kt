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

package net.liyze.basin.service

import net.liyze.basin.resource.AbstractBean
import net.liyze.basin.resource.BeanManager
import net.liyze.basin.resource.exception.BeanNotFoundException
import java.util.concurrent.ConcurrentHashMap

object ServiceManager {
    val services: MutableMap<String, MutableMap<String, Class<*>>> = ConcurrentHashMap()
    fun getService(type: String, id: String): Any {
        services[type]?.get(id)?.let {
            return BeanManager.getBean(it)
        }
        throw BeanNotFoundException("$type:$id")
    }

    fun register(type: String, id: String, clazz: Class<*>) {
        val map = services[type]
        if (map == null) {
            services[type] = ConcurrentHashMap(mapOf(id to clazz))
        } else {
            map[id] = clazz
        }
    }

    fun <T> register(type: String, id: String, bean: AbstractBean<T>) {
        BeanManager.addBean(bean)
        register(type, id, bean.type)
    }
}