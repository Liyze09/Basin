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
    }
}