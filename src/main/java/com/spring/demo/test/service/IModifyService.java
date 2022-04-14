package com.spring.demo.test.service;

public interface IModifyService {
    public String add(String name, String addr);

    public String edit(Integer id, String name);

    public String remove(Integer id) throws Exception;
}
