package com.spring.demo.test.controller;

import com.spring.demo.mvc.GPModelAndView;
import com.spring.demo.mvc.annotation.GPAutowired;
import com.spring.demo.mvc.annotation.GPController;
import com.spring.demo.mvc.annotation.GPRequestMapping;
import com.spring.demo.mvc.annotation.GPRequestParam;
import com.spring.demo.test.service.IModifyService;
import com.spring.demo.test.service.IQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GPController
@GPRequestMapping("/web")
public class MyAction {
    @GPAutowired
    IQueryService queryService;
    @GPAutowired
    IModifyService modifyService;

    @GPRequestMapping("/query.json")
    public GPModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @GPRequestParam("name") String name) {
        return out(response, queryService.query(name));
    }

    @GPRequestMapping("/add*.json")
    public GPModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @GPRequestParam("name") String name,
                              @GPRequestParam("addr") String addr) {
        return out(response, modifyService.add(name, addr));
    }

    @GPRequestMapping("/remove.json")
    public GPModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @GPRequestParam("id") Integer id) throws Exception {
        return out(response, modifyService.remove(id));
    }

    @GPRequestMapping("/edit.json")
    public GPModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                               @GPRequestParam("id") Integer id,
                               @GPRequestParam("name") String name) {
        return out(response, modifyService.edit(id, name));
    }


    private GPModelAndView out(HttpServletResponse resp, String str) {
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
