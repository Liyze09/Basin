package net.liyze.basin.resource

@FunctionalInterface
fun interface Factory<T> {
    fun get(): T
}