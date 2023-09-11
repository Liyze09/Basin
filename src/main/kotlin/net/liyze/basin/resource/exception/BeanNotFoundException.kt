package net.liyze.basin.resource.exception

class BeanNotFoundException(beanName: String) : RuntimeException(beanName)