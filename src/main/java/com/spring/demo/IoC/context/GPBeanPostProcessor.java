package com.spring.demo.IoC.context;

//依赖注入DI
public class GPBeanPostProcessor {

    //为在bena的初始化之前提供回调入口
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws
            Exception {
        return bean;
    }

    //为在bean的初始化之后提供回调入口
    public Object postProcessAfterInitialization(Object bean, String beanName) throws
            Exception {
        return bean;
    }

}
