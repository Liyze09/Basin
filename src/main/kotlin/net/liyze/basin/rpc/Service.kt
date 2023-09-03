package net.liyze.basin.rpc

@FunctionalInterface
fun interface Service {
    fun body(arg: Any): Any
}