package net.liyze.basin.context.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class ComponentScan(
    /**
     * Package names to scan. Default to current package.
     */
    vararg val value: String = []
)
