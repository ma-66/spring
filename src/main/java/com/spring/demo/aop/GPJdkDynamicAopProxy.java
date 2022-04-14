package com.spring.demo.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 使用JDK Proxy API生成代理
 */
public class GPJdkDynamicAopProxy implements GPAopProxy, InvocationHandler {
    private GPAdvisedSupport config;

    public GPJdkDynamicAopProxy(GPAdvisedSupport config) {
        this.config = config;
    }

    //把原生对象传进来
    @Override
    public Object getProxy() {
        return getProxy(this.config.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return Proxy.newProxyInstance(classLoader, this.config.getTargetClass().getInterfaces(), this);
    }

    //invoke()方法是执行代理的关键入口
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //将每一个JoinPoint也就是被代理的业务方法（Method）封装成一个拦截器，组合成一个拦截器链
        List<Object> interceptorsAndDynamicMethodMatchers =
                config.getInterceptorsAndDynamicInterceptionAdvice(method, this.config.getTargetClass());
        //交给拦截器链MethodInvocation的proceed()方法执行
        GPMethodInvocation invocation = new GPMethodInvocation(proxy, this.config.getTargetClass(),
                method, args, this.config.getTargetClass(), interceptorsAndDynamicMethodMatchers);
        return invocation.proceed();
    }
}
