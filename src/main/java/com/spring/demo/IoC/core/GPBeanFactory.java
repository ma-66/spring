package com.spring.demo.IoC.core;

/**
 * 单例工厂的顶层设计
 */
public interface GPBeanFactory {
    /**
     * 根据beanName从IoC容器中获取一个实例bean
     */
    Object getBean(String beanName) throws Exception;

    public Object getBean(Class<?> beanClass) throws Exception;
}
