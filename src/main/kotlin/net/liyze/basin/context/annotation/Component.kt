package net.liyze.basin.context.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Component(
    /**
     * Bean name. Default to simple class name with first-letter-lowercase.
     */
    val value: String = ""
)
