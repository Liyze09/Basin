package net.liyze.basin.context.exception

class NoSuchBeanDefinitionException : BeanDefinitionException {
    constructor()
    constructor(message: String?) : super(message)
}
