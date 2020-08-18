package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * PostgreSQL 数据源
 */
public class PostgreSQLDataSource extends SqlDataSource {

    public PostgreSQLDataSource(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public PostgreSQLDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        super(jdbcTemplate, storeApi);
    }

    @Override
    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page) {
        return "select count(1) from ("+script+") t1";
    }

    @Override
    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager, Page page) {
        Integer offset = apiPager.getIndexVarValue(page.getPageSize(),page.getPageNo());
        return script + " limit "+page.getPageSize()+" offset "+offset;
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}
