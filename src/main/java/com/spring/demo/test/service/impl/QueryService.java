package com.spring.demo.test.service.impl;

import com.spring.demo.mvc.annotation.GPService;
import com.spring.demo.test.service.IQueryService;

import java.text.SimpleDateFormat;
import java.util.Date;

@GPService
public class QueryService implements IQueryService {
    @Override
    public String query(String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sdf.format(new Date());
        String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
        return json;
    }
}
