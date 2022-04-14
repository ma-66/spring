package com.spring.demo.aop;

import java.lang.reflect.Method;

/**
 * 异常通知具体实现
 */
public class GPAfterThrowingAdvice extends GPAbstractAspectJAdvice implements GPAdvice,
        GPMethodInterceptor {

    private String throwingName;
    private GPMethodInvocation mi;

    public GPAfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public void setThrowingName(String name) {
        this.throwingName = name;
    }

    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch (Throwable ex) {
            invokeAdviceMethod(mi, null, ex.getCause());
            throw ex;
        }
    }
}
