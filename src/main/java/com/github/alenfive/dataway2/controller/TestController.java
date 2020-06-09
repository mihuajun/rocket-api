package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.entity.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
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
}
