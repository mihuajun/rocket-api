package com.github.alenfive.rocketapi.controller;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试类
 */
@RestController
@RequestMapping
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping(value = {"/hello/{hello}99"})
    public Object test(String id,
                       String name,
                       @PathVariable String hello,
                       ApiInfo apiInfo){
        return jdbcTemplate.queryForList("select * from user");
    }

    @GetMapping(value = {"/hello/test1"})
    public Object test11(){
        return "hello";
    }

    @GetMapping(value = {"/hello/test2"})
    public Object test2(HttpServletRequest request){

        Set<MediaType> mediaTypeSet = new HashSet<>();
        mediaTypeSet.add(MediaType.ALL);
        request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypeSet);
        return null;
    }
}
