package net.liyze.basin.event

@FunctionalInterface
fun interface Observer {
    fun run(event: Any)
}

fun interface RpcObserver : Observer {
    override fun run(event: Any) {
        response(event)
    }

    fun response(event: Any): Any
}