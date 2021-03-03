package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * mongodb 数据源操作
 */
public class ClickHouseDataSource extends DataSourceDialect {

    protected JdbcTemplate jdbcTemplate;

    protected NamedParameterJdbcTemplate parameterJdbcTemplate;

    private ClickHouseDataSource(){}

    public ClickHouseDataSource(JdbcTemplate jdbcTemplate) {
        this.storeApi = false;
        this.jdbcTemplate = jdbcTemplate;
        this.parameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    void saveApiInfo(ApiInfo apiInfo) {

    }

    @Override
    public ApiInfo findApiInfoById(ApiInfo apiInfo) {
        return null;
    }

    @Override
    public void deleteApiInfo(ApiInfo apiInfo) {

    }

    @Override
    public void updateApiInfo(ApiInfo apiInfo) {

    }

    @Override
    public List<ApiInfo> listApiInfoByEntity(ApiInfo apiInfo) {
        return null;
    }

    @Override
    public void saveApiInfoHistory(ApiInfoHistory apiInfoHistory) {

    }

    @Override
    public List<ApiInfoHistory> listApiInfoHistoryByEntity(ApiInfoHistory apiInfoHistory, IApiPager apiPager, Page page) {
        return null;
    }

    @Override
    public void saveApiExample(ApiExample apiExample) {

    }

    @Override
    public List<ApiExample> listApiExampleByEntity(ApiExample apiExample, IApiPager apiPager, Page page) {
        return null;
    }

    @Override
    public void deleteExample(ApiExample apiExample) {

    }

    @Override
    public void saveApiConfig(ApiConfig apiConfig) {

    }

    @Override
    public void updateApiConfig(ApiConfig apiConfig) {

    }

    @Override
    public List<ApiConfig> listApiConfigByEntity(ApiConfig apiConfig) {
        return null;
    }

    @Override
    public List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(script.toString());
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        throw new UnsupportedOperationException("The operation is not allowed");
    }

    @Override
    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        throw new UnsupportedOperationException("The operation is not allowed");
    }

    @Override
    public Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement(script.toString(), Statement.RETURN_GENERATED_KEYS);
            return ps;
        };
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        return keyHolder.getKeyList().stream().map(item->item.get("GENERATED_KEY")).collect(Collectors.toList());
    }

    @Override
    public String buildCountScript(String script, ApiInfo apiInfo, ApiParams apiParams, IApiPager apiPager,Page page) {
        return script;
    }

    @Override
    public String buildPageScript(String script, ApiInfo apiInfo, ApiParams apiParams,  IApiPager apiPager,Page page) {
        return script;
    }

    @Override
    public String transcoding(String param) {
        return param
                .replace("\'","\\\'");
    }

    @Override
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}
