package com.github.alenfive.dataway2.datasource;

import com.github.alenfive.dataway2.datasource.DataSourceDialect;
import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/27 17:19
 * @UpdateDate: 2020/5/27 17:19
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu MYSQL数据源
 */
@Transactional
public class SqlDataSource extends DataSourceDialect {

    private JdbcTemplate jdbcTemplate;

    public SqlDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        this.storeApi = storeApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String listApiInfoScript() {
        return "select id,method,path,datasource,`type`,`group`,editor,`comment`,script,params,create_time,update_time from api_info where service = #{service}";
    }

    @Override
    public String getApiInfoScript() {
        return "select id,method,path,datasource,`type`,`group`,editor,`comment`,script,params,create_time,update_time from api_info where method = #{method} and path = #{path}";
    }

    @Override
    public String saveApiInfoScript() {
        return "insert into api_info(method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,create_time,update_time) values(#{method},#{path},#{datasource},#{type},#{service},#{group},#{editor},#{comment},#{script},#{createTime},#{updateTime})";
    }

    @Override
    public String updateApiInfoScript() {
        return "update api_info set method=#{method},path=#{path},datasource=#{datasource},`group`=#{group},editor=#{editor},`comment`=#{comment},script=#{script},update_time=#{updateTime} where id = #{id}";
    }

    @Override
    public String deleteApiInfoScript() {
        return "delete from api_info where id = #{id}";
    }

    @Override
    String saveApiExampleScript() {
        return "insert into api_example(api_info_id,method,url,request_header,request_body,response_header,response_body,status,time,options,create_time) " +
                "values(#{apiInfoId},#{method},#{url},#{requestHeader},#{requestBody},#{responseHeader},#{responseBody},#{status},#{time},#{options},#{createTime})";
    }

    @Override
    String lastApiExampleScript() {
        return "select id,api_info_id,method,url,request_header,request_body,response_header,response_body,status,time,options,create_time from where api_info_id = #{apiInfoId} order by id desc limit #{limit}";
    }

    @Override
    String deleteExampleScript() {
        return "delete api_info_id where id in (#{ids})";
    }

    @Override
    public List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(script.toString());
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    Object insert(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        PreparedStatementCreator preparedStatementCreator = con -> {
            PreparedStatement ps = con.prepareStatement(script.toString(), Statement.RETURN_GENERATED_KEYS);
            return ps;
        };
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        return keyHolder.getKey();
    }
}
