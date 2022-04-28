package com.spring.demo.aop;

import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;

/**
 * 使用CGlib API生成代理类，在此不举例
 * 感兴趣的可以自行实现
 */
public class GPCglibAopProxy implements GPAopProxy, InvocationHandler {
    private GPAdvisedSupport config;

    public GPCglibAopProxy(GPAdvisedSupport config) {
        this.config = config;
    }

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
