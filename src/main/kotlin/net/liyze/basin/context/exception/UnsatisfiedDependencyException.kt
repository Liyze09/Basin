package net.liyze.basin.context.exception

class UnsatisfiedDependencyException : BeanCreationException {
    constructor()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
