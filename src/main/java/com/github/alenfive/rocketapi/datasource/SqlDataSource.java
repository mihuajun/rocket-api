package com.github.alenfive.rocketapi.datasource;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.entity.vo.TableInfo;
import com.github.alenfive.rocketapi.extend.IApiPager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关系型数据源，`JdbcTemplate`所操作的数据源
 */
public class SqlDataSource extends DataSourceDialect {

    protected JdbcTemplate jdbcTemplate;

    private SqlDataSource(){}

    public SqlDataSource(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SqlDataSource(JdbcTemplate jdbcTemplate, boolean storeApi) {
        this.storeApi = storeApi;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String listApiInfoScript() {
        return "select id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time,update_time from api_info where service = #{service}";
    }

    @Override
    String lastApiInfoHistoryScript() {
        return "select id,api_info_id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time from api_info_history where service = #{service} ?{apiInfoId,and api_info_id = #{apiInfoId}} order by id desc limit #{index},#{pageSize}";
    }

    @Override
    public String saveApiInfoHistoryScript() {
        return "insert into api_info_history(id,api_info_id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time) values(#{id},#{apiInfoId},#{method},#{path},#{datasource},#{type},#{service},#{group},#{editor},#{comment},#{script},#{options},#{createTime})";
    }

    @Override
    public String getApiInfoScript() {
        return "select id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time,update_time from api_info where id = #{id}";
    }

    @Override
    public String saveApiInfoScript() {
        return "insert into api_info(id,method,path,datasource,`type`,`service`,`group`,editor,`comment`,script,options,create_time,update_time) values(#{id},#{method},#{path},#{datasource},#{type},#{service},#{group},#{editor},#{comment},#{script},#{options},#{createTime},#{updateTime})";
    }

    @Override
    public String updateApiInfoScript() {
        return "update api_info set method=#{method},path=#{path},datasource=#{datasource},`service`=#{service},`group`=#{group},editor=#{editor},`comment`=#{comment},script=#{script},options=#{options},update_time=#{updateTime} where id = #{id}";
    }

    @Override
    public String deleteApiInfoScript() {
        return "delete from api_info where id = #{id}";
    }

    @Override
    public String saveApiExampleScript() {
        return "insert into api_example(id,api_info_id,method,url,request_header,request_body,response_header,response_body,status,time,options,editor,create_time) " +
                "values(#{id},#{apiInfoId},#{method},#{url},#{requestHeader},#{requestBody},#{responseHeader},#{responseBody},#{status},#{time},#{options},#{editor},#{createTime})";
    }

    @Override
    public String lastApiExampleScript() {
        return "select id,api_info_id,method,url,request_header,request_body,response_header,response_body,status,time,options,editor,create_time from api_example where api_info_id = #{apiInfoId} order by id desc limit #{limit}";
    }

    @Override
    public String deleteExampleScript() {
        return "delete from api_example where id in (#{ids})";
    }

    @Override
    public List<Map<String,Object>> find(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(script.toString());
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    public Long update(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
    }

    @Override
    public Long remove(StringBuilder script, ApiInfo apiInfo, ApiParams apiParams) {
        return Long.valueOf(jdbcTemplate.update(script.toString()));
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
    public List<TableInfo> buildTableInfo() {
        return null;
    }
}
