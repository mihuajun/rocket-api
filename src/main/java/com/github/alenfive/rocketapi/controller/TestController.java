package com.github.alenfive.rocketapi.controller;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/22 12:57
 * @UpdateDate: 2020/5/22 12:57
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu test
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

    @GetMapping(value = {"/hello/a112"})
    public Object test11(String id,
                       String name,
                       ApiInfo apiInfo){
        return "hello";
    }
}
