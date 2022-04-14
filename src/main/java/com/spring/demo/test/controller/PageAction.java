package com.spring.demo.test.controller;

import com.spring.demo.mvc.GPModelAndView;
import com.spring.demo.mvc.annotation.GPAutowired;
import com.spring.demo.mvc.annotation.GPController;
import com.spring.demo.mvc.annotation.GPRequestMapping;
import com.spring.demo.mvc.annotation.GPRequestParam;
import com.spring.demo.test.service.IQueryService;
import java.util.HashMap;
import java.util.Map;

@GPController
@GPRequestMapping("/")
public class PageAction {
    @GPAutowired
    IQueryService queryService;

    @GPRequestMapping("/html/first.html")
    public GPModelAndView query(@GPRequestParam("teacher") String teacher) {
        String result = queryService.query(teacher);
        Map<String, Object> model = new HashMap<>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new GPModelAndView("/first.html", model);
    }


}
