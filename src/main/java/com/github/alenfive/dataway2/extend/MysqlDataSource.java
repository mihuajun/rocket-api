package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @menu MYSQL数据源
 */
@Service
@Transactional
public class MysqlDataSource implements DataSourceDialect {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public String listApiInfoScript() {
        return "select id,method,path,datasource,`type`,`group`,editor,`comment`,script,params,create_time,update_time from api_info";
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
        jdbcTemplate.execute(script);
        return null;
    }

    @Override
    public List<Map<String,Object>> executeQuery(String script, ApiInfo apiInfo, ApiParams apiParams) {
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(script);
        return resultList.stream().map(this::toReplaceKeyLow).collect(Collectors.toList());
    }

    @Override
    public Long executeCount(String script, ApiInfo apiInfo, ApiParams apiParams) {
        return jdbcTemplate.queryForObject(script,Long.class);
    }

    private Map<String,Object> toReplaceKeyLow(Map<String,Object> map){
        Map<String,Object> result = new HashMap<>(map.size());
        for(String key : map.keySet()){
            result.put(underlineToCamel(key),map.get(key));
        }
        return result;
    }

    private String underlineToCamel(String name){
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ('_' == c) {
                if (++i < name.length()){
                    sb.append(Character.toUpperCase(name.charAt(i)));
                }
            }else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
