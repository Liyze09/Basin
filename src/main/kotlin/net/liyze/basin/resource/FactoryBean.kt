package net.liyze.basin.resource

class FactoryBean<T>(
    override val type: Class<T>,
    val factory: Factory<T>
) : AbstractBean<T>() {
    override fun getInstance(): T {
        return factory.get()
    }
}