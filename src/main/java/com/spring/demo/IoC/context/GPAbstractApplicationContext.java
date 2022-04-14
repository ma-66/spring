package com.spring.demo.IoC.context;

/**
 * IoC容器实现的顶层设计
 */
public abstract class GPAbstractApplicationContext {
    //受保护，只提供给子类重写
    public void refresh() throws Exception {
    }
}
