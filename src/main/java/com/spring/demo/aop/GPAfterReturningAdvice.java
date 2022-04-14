package com.spring.demo.aop;

import java.lang.reflect.Method;

/**
 * 后置通知具体实现
 */
public class GPAfterReturningAdvice extends GPAbstractAspectJAdvice implements GPAdvice,
        GPMethodInterceptor {

    private GPJoinPoint joinPoint;

    public GPAfterReturningAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }

    public void afterReturning(Object returnValue, Method method, Object[] args, Object target)
            throws Throwable {
        invokeAdviceMethod(joinPoint, returnValue, null);

    }
}
