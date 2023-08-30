package net.liyze.basin.context.annotation

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Autowired(
    /**
     * Is required.
     */
    val value: Boolean = true,
    /**
     * Bean name if set.
     */
    val name: String = ""
)
