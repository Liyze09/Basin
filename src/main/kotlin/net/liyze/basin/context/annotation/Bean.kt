package net.liyze.basin.context.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Bean(
    /**
     * Bean name. default to method name.
     */
    val value: String = "", val initMethod: String = "", val destroyMethod: String = ""
)
