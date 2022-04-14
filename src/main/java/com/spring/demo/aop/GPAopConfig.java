package com.spring.demo.aop;

import lombok.Data;

/**
 * AOP配置封装
 */
@Data
public class GPAopConfig {
    //以下配置与配置文件中的属性一一对应
    private String pointCut;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectClass;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
