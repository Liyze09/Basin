package net.liyze.basin.context.exception

class NoUniqueBeanDefinitionException : BeanDefinitionException {
    constructor()
    constructor(message: String?) : super(message)
}
