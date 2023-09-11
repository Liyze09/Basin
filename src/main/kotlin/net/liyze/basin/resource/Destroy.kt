package net.liyze.basin.resource

fun interface Destroy<T> {
    fun destroy(instance: T)
}