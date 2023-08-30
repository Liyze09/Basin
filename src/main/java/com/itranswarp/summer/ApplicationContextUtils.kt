@file: JvmName("ApplicationContextUtils")

package com.itranswarp.summer

import java.util.*

@JvmField
var applicationContext: ApplicationContext? = null
val requiredApplicationContext: ApplicationContext
    get() = Objects.requireNonNull(applicationContext, "ApplicationContext is not set.")!!
