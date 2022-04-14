package com.spring.demo.IoC.context;

import com.spring.demo.IoC.beans.GPBeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {
    //存储注册信息的beanDefinition
    protected final Map<String, GPBeanDefinition> beanDefinitionMap = new
            ConcurrentHashMap<String, GPBeanDefinition>();
}
