package net.liyze.basin.context

import net.liyze.basin.context.exception.BeanCreationException
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.*

class BeanDefinition : Comparable<BeanDefinition> {
    // unique bean name:
    @JvmField
    val name: String

    // bean class:
    @JvmField
    val beanClass: Class<*>

    // constructor or null:
    @JvmField
    val constructor: Constructor<*>?

    // factory name or null:
    @JvmField
    val factoryName: String?

    // factory method or null:
    @JvmField
    val factoryMethod: Method?

    // bean order used by ApplicationContext.getBeans(type):
    private val order: Int

    // has @Primary?
    val isPrimary: Boolean

    // bean instance:
    var instance: Any? = null
        private set

    // autowired and called init method:
    var isInit = false
        private set
    var initMethodName: String? = null
        private set
    var destroyMethodName: String? = null
        private set
    var initMethod: Method? = null
        private set
    var destroyMethod: Method? = null
        private set

    constructor(
        name: String,
        beanClass: Class<*>,
        constructor: Constructor<*>,
        order: Int,
        primary: Boolean,
        initMethodName: String?,
        destroyMethodName: String?,
        initMethod: Method?,
        destroyMethod: Method?
    ) {
        this.name = name
        this.beanClass = beanClass
        this.constructor = constructor
        factoryName = null
        factoryMethod = null
        this.order = order
        isPrimary = primary
        constructor.setAccessible(true)
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod)
    }

    constructor(
        name: String,
        beanClass: Class<*>,
        factoryName: String?,
        factoryMethod: Method,
        order: Int,
        primary: Boolean,
        initMethodName: String?,
        destroyMethodName: String?,
        initMethod: Method?,
        destroyMethod: Method?
    ) {
        this.name = name
        this.beanClass = beanClass
        constructor = null
        this.factoryName = factoryName
        this.factoryMethod = factoryMethod
        this.order = order
        isPrimary = primary
        factoryMethod.setAccessible(true)
        setInitAndDestroyMethod(initMethodName, destroyMethodName, initMethod, destroyMethod)
    }

    private fun setInitAndDestroyMethod(
        initMethodName: String?,
        destroyMethodName: String?,
        initMethod: Method?,
        destroyMethod: Method?
    ) {
        this.initMethodName = initMethodName
        this.destroyMethodName = destroyMethodName
        initMethod?.setAccessible(true)
        destroyMethod?.setAccessible(true)
        this.initMethod = initMethod
        this.destroyMethod = destroyMethod
    }

    fun setInstance(instance: Any) {
        Objects.requireNonNull(instance, "Bean instance is null.")
        if (!beanClass.isAssignableFrom(instance.javaClass)) {
            throw BeanCreationException(
                String.format(
                    "Instance '%s' of Bean '%s' is not the expected type: %s", instance, instance.javaClass.getName(),
                    beanClass.getName()
                )
            )
        }
        this.instance = instance
    }

    val requiredInstance: Any?
        get() {
            if (instance == null) {
                throw BeanCreationException(
                    String.format(
                        "Instance of bean with name '%s' and type '%s' is not instantiated during current stage.",
                        name, beanClass.getName()
                    )
                )
            }
            return instance
        }

    fun setInit() {
        isInit = true
    }

    override fun toString(): String {
        return ("BeanDefinition [name=" + name + ", beanClass=" + beanClass.getName() + ", factory=" + createDetail + ", init-method="
                + (if (initMethod == null) "null" else initMethod!!.name) + ", destroy-method=" + (if (destroyMethod == null) "null" else destroyMethod!!.name)
                + ", primary=" + isPrimary + ", instance=" + instance + "]")
    }

    val createDetail: String?
        get() {
            if (factoryMethod != null) {
                val params = java.lang.String.join(
                    ", ",
                    *Arrays.stream(factoryMethod.parameterTypes)
                        .map { obj: Class<*> -> obj.getSimpleName() }
                        .toArray { Array(0) { String() } })
                return factoryMethod.declaringClass.getSimpleName() + "." + factoryMethod.name + "(" + params + ")"
            }
            return null
        }

    override fun compareTo(other: BeanDefinition): Int {
        val cmp = order.compareTo(other.order)
        return if (cmp != 0) {
            cmp
        } else name.compareTo(other.name)
    }
}
