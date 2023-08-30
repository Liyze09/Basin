package net.liyze.basin.http.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Control(val serverName: String)
