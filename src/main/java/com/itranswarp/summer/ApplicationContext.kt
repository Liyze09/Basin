package com.itranswarp.summer

/**
 * Used for BeanPostProcessor.
 */
interface ApplicationContext : AutoCloseable, Cloneable {
    /**
     * Is the bean existed?
     */
    fun containsBean(name: String): Boolean

    /**
     * Return the unique bean by name
     * if it does not exist, will throw NoSuchBeanDefinitionException
     */
    fun <T> getBean(name: String): T

    /**
     * Return the unique bean by name
     * if it does not exist, will throw NoSuchBeanDefinitionException
     * if bean is not required type, will throw BeanNotOfRequiredTypeException
     */
    fun <T> getBean(name: String, requiredType: Class<T>): T

    /**
     * Return the unique bean by type
     * if it does not exist, will throw NoSuchBeanDefinitionException
     * @param requiredType the type of beans
     * @return the unique bean
     */
    fun <T> getBean(requiredType: Class<T>): T

    /**
     * Return a group of bean by type
     * if it does not exist, will return an empty list
     * @param requiredType the type of beans
     * @return List of beans
     */
    fun <T> getBeans(requiredType: Class<T>): List<T>?

    /**
     * Close the context and call beans' destroy method
     */
    override fun close()
    fun findBeanDefinitions(type: Class<*>): List<BeanDefinition?>?
    fun findBeanDefinition(type: Class<*>): BeanDefinition?
    fun findBeanDefinition(name: String): BeanDefinition?
    fun findBeanDefinition(name: String, requiredType: Class<*>?): BeanDefinition?
    fun createBeanAsEarlySingleton(def: BeanDefinition): Any?
    val beanDefinitions: Collection<BeanDefinition?>?
}
