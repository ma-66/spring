package com.spring.demo.aop;

/**
 * 使用CGlib API生成代理类，在此不举例
 * 感兴趣的可以自行实现
 */
public class GPCglibAopProxy implements GPAopProxy {
    private GPAdvisedSupport config;

    public GPCglibAopProxy(GPAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
