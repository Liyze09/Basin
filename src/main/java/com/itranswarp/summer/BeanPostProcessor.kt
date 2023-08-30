package com.itranswarp.summer

interface BeanPostProcessor {
    /**
     * Invoked after new Bean().
     */
    fun postProcessBeforeInitialization(bean: Any?, beanName: String?): Any? {
        return bean
    }

    /**
     * Invoked after bean.init() called.
     */
    fun postProcessAfterInitialization(bean: Any?, beanName: String): Any? {
        return bean
    }

    /**
     * Invoked before bean.setXyz() called.
     */
    fun postProcessOnSetProperty(bean: Any?, beanName: String): Any? {
        return bean
    }
}
