package net.liyze.basin.context.exception

open class NestedRuntimeException : RuntimeException {
    constructor()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
