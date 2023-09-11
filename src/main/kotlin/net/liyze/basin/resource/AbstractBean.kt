package net.liyze.basin.resource

sealed class AbstractBean<T> {
    abstract val type: Class<out T>
    abstract fun getInstance(): T
    open fun destroy() {}
}