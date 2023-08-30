@file: JvmName("ApplicationContextUtils")

package net.liyze.basin.context

import java.util.*

@JvmField
var applicationContext: ApplicationContext? = null
val requiredApplicationContext: ApplicationContext
    get() = Objects.requireNonNull(applicationContext, "ApplicationContext is not set.")!!
