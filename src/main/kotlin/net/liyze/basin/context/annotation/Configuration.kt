package net.liyze.basin.context.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Component
annotation class Configuration(
    /**
     * Bean name. Default to simple class name with first-letter-lower-case.
     */
    val value: String = ""
)
