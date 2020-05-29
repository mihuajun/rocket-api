package com.github.alenfive.dataway2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/5/24 21:08
 * @UpdateDate: 2020/5/24 21:08
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu API服务
 */
@Transactional
@Service
public class ApiService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScriptParseService parseService;

    public Object execute() {
        return "hello";
    }
}
