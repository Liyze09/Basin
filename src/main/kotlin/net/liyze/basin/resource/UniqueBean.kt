package net.liyze.basin.resource

import net.liyze.basin.util.createInstance

class UniqueBean<T>(
    override val type: Class<T>,
) : AbstractBean<T>() {
    override fun getInstance(): T {
        return type.createInstance()
    }
}