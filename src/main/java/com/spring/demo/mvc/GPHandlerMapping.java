package com.spring.demo.mvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class GPHandlerMapping {
    private Object controller;//目标方法所在的controller对象
    private Method method;//URL对应的目标方法
    private Pattern pattern;//URl的封装

    public GPHandlerMapping(Pattern pattern, Object controller, Method method) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
