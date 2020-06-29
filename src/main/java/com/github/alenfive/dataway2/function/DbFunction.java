package com.github.alenfive.dataway2.function;

import com.github.alenfive.dataway2.datasource.DataSourceManager;
import com.github.alenfive.dataway2.extend.ApiInfoContent;
import com.github.alenfive.dataway2.script.IScriptParse;
import com.github.alenfive.dataway2.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @Description:脚本运行
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 17:58
 * @UpdateDate: 2020/6/23 17:58
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Component
@Slf4j
public class DbFunction implements IFunction{

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private ScriptParseService parseService;

    @Override
    public String getVarName() {
        return "db";
    }

    public Long count(String script,String dataSource){
        List<Map<String,Object>> list = find(script,dataSource);
       if (CollectionUtils.isEmpty(list))return 0L;

       Object count = list.get(0).values().toArray()[0];
       if (count == null){
           count = list.size();
       }
       return Long.valueOf(count.toString());
    }

    public Map<String,Object> findOne(String script,String dataSource){
        List<Map<String,Object>> list = find(script,dataSource);
        if (list.size() == 0)return null;
        return list.get(0);
    }

    public List<Map<String,Object>> find(String script,String dataSource){
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        List<Map<String,Object>> result = dataSourceManager.find(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Object insert(String script,String dataSource){
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        Object result = dataSourceManager.insert(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Object remove(String script,String dataSource){
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        Object result =  dataSourceManager.remove(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Long update(String script,String dataSource){
        StringBuilder sbScript = new StringBuilder(script);
        parseService.parse(sbScript,apiInfoContent.getApiParams());
        Long result =  dataSourceManager.update(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        log.info("generate script:{}",sbScript);
        return result;
    }

    public Long count(String script){
        return this.count(script,null);
    }

    public Map<String,Object> findOne(String script){
        return this.findOne(script,null);
    }

    public List<Map<String,Object>> find(String script){
        return this.find(script,null);
    }

    public Object insert(String script){
        return this.insert(script,null);
    }

    public Object remove(String script){
        return this.remove(script,null);
    }

    public Long update(String script){
        return this.update(script,null);
    }

}
