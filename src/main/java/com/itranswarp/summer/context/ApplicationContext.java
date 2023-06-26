package com.itranswarp.summer.context;

import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * Used for BeanPostProcessor.
 */
public interface ApplicationContext extends AutoCloseable {
    /**
     * 是否存在指定name的Bean？
     */
    boolean containsBean(String name);

    /**
     * 根据name返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
     */
    <T> T getBean(String name);

    /**
     * 根据name返回唯一Bean，未找到抛出NoSuchBeanDefinitionException，找到但type不符抛出BeanNotOfRequiredTypeException
     */
    <T> T getBean(String name, Class<T> requiredType);

    /**
     * 根据type返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
     */
    <T> T getBean(Class<T> requiredType);

    /**
     * 根据type返回一组Bean，未找到返回空List
     */
    <T> List<T> getBeans(Class<T> requiredType);

    /**
     * 关闭并执行所有bean的destroy方法
     */
    void close();


    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    Object createBeanAsEarlySingleton(BeanDefinition def);
}
