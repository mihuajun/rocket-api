package com.github.alenfive.rocketapi.datasource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/8/17 15:49
 * @UpdateDate: 2020/8/17 15:49
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
public abstract class ISQLDialect implements IDataSourceDialect{
    protected JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
