package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/5/27 17:19
 * @UpdateDate: 2020/5/27 17:19
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu mongo数据源
 */
@Transactional
public class MongoDataSource extends DataSourceDialect {

    private MongoTemplate mongoTemplate;

    public MongoDataSource(MongoTemplate mongoTemplate,boolean storeApi) {
        this.mongoTemplate = mongoTemplate;
        this.storeApi = storeApi;
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
    public Object execute(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return null;
    }

    @Override
    public List<Map<String,Object>> executeQuery(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return null;
    }

    @Override
    public Long executeCount(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return null;
    }


}
