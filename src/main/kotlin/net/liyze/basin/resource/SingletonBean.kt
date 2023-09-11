package net.liyze.basin.resource

class SingletonBean<T>(
    val singleton: T,
    override val type: Class<out T> = singleton!!::class.java,
    val destroy: Destroy<T> = Destroy {}
) : AbstractBean<T>() {
    override fun getInstance(): T {
        return singleton
    }

    override fun destroy() {
        destroy.destroy(singleton)
    }
}