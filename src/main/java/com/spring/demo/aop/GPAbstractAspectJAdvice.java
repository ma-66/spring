package com.spring.demo.aop;

import java.lang.reflect.Method;

/**
 * 封装拦截器回调的通用逻辑，在Mini版本中主要封装了反射动态调用方法
 */
public abstract class GPAbstractAspectJAdvice implements GPAdvice {
    private Method aspectMethod;
    private Object aspectTarget;

    public GPAbstractAspectJAdvice(
            Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    //反射动态调用方法
    protected Object invokeAdviceMethod(GPJoinPoint joinPoint, Object returnValue, Throwable ex)
            throws Throwable {
        Class<?>[] paramsTypes = this.aspectMethod.getParameterTypes();
        if (null == paramsTypes || paramsTypes.length == 0) {
            return this.aspectMethod.invoke(aspectTarget);
        } else {
            Object[] args = new Object[paramsTypes.length];
            for (int i = 0; i < paramsTypes.length; i++) {
                if (paramsTypes[i] == GPJoinPoint.class) {
                    args[i] = joinPoint;
                } else if (paramsTypes[i] == Throwable.class) {
                    args[i] = ex;
                } else if (paramsTypes[i] == Object.class) {
                    args[i] = returnValue;
                }
            }
            return this.aspectMethod.invoke(aspectTarget, args);
        }
    }
}
