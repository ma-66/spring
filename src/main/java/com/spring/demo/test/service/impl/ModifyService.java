package com.spring.demo.test.service.impl;

import com.spring.demo.mvc.annotation.GPService;
import com.spring.demo.test.service.IModifyService;

@GPService
public class ModifyService implements IModifyService {
    @Override
    public String add(String name, String addr) {
        return "modifyService add,name=" + name + ",addr=" + addr;
    }

    @Override
    public String edit(Integer id, String name) {
        return "modifyService edit,id=" + id + ",name=" + name;
    }

    @Override
    public String remove(Integer id) throws Exception {
        throw new Exception("故意抛出异常，测试切面通知是否生效");
//        return "modifyService remove,id="+id;
    }
}
