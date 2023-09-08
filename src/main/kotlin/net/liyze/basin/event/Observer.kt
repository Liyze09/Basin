package net.liyze.basin.event

@FunctionalInterface
fun interface Observer {
    fun run(event: Any)
}