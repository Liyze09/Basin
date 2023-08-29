@file: JvmName("ContextUtils")

package net.liyze.basin.util

import com.itranswarp.summer.BeanDefinition
import net.liyze.basin.core.contexts

fun getBean(name: String): BeanDefinition? {
    for (context in contexts) {
        val bean = context.findBeanDefinition(name)
        if (bean != null) return bean
    }
    return null
}

