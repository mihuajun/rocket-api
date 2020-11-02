package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiPager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作函数
 */
@SuppressWarnings("DuplicatedCode")
@Component
@Slf4j
public class DbFunction implements IFunction{

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private IApiPager apiPager;

    @Autowired
    private UtilsFunction utilsFunction;

    @Override
    public String getVarName() {
        return "db";
    }

    private String parseSql(String script){
        if (script.startsWith("sql")){
            script = script.substring(3);
        }
        if (script.startsWith("\n")){
            script = script.substring(1);
        }
        if (script.endsWith("\n")){
            script = script.substring(0,script.length()-1);
        }
        return script;
    }

    public Long count(String script,String dataSource) throws Exception {
        script = parseSql(script);
        List<Map<String,Object>> list = find(script,dataSource);
        if (CollectionUtils.isEmpty(list))return 0L;
        if (list.size()>1){
            return Long.valueOf(list.size());
        }
        Object[] fieldValues = list.get(0).values().toArray();
        if (fieldValues.length>1 || !(fieldValues[0] instanceof Number)){
            return 1L;
        }

        return Long.valueOf(fieldValues[0].toString());
    }

    public Map<String,Object> findOne(String script,String dataSource) throws Exception {
        script = parseSql(script);
        List<Map<String,Object>> list = find(script,dataSource);
        if (list.size() == 0)return null;
        return list.get(0);
    }

    public List<Map<String,Object>> find(String script,String dataSource) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(script);
        List<Map<String,Object>> result = null;
        try {
            result = dataSourceManager.find(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
        }

        return result;
    }

    public Object insert(String script,String dataSource) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(script);
        Object result = null;
        try {
            result = dataSourceManager.insert(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
        }
        return result;
    }

    public Object remove(String script,String dataSource) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(script);
        Object result =  null;
        try {
            result = dataSourceManager.remove(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
        }
        return result;
    }

    public Long update(String script,String dataSource) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(script);
        Long result =  null;
        try {
            result = dataSourceManager.update(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
        }
        return result;
    }

    public Object pager(String script,String dataSource) throws Exception {
        script = parseSql(script);
        Page page = Page.builder()
                .pageNo(Integer.valueOf(utilsFunction.val(apiPager.getPageNoVarName()).toString()))
                .pageSize(Integer.valueOf(utilsFunction.val(apiPager.getPageSizeVarName()).toString()))
                .build();
        String totalSql = dataSourceManager.buildCountScript(script,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,apiPager,page);
        Long total = this.count(totalSql,dataSource);
        List<Map<String,Object>> data = null;
        if (total > 0){
            String pageSql = dataSourceManager.buildPageScript(script,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,apiPager,page);
            data = this.find(pageSql,dataSource);
        }else{
            data = Collections.emptyList();
        }
        return apiPager.buildPager(total,data,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams());
    }

    public Object pager(String script) throws Exception {
        script = parseSql(script);
        return this.pager(script,null);
    }

    public Long count(String script) throws Exception {
        script = parseSql(script);
        return this.count(script,null);
    }

    public Map<String,Object> findOne(String script) throws Exception {
        script = parseSql(script);
        return this.findOne(script,null);
    }

    public List<Map<String,Object>> find(String script) throws Exception {
        script = parseSql(script);
        return this.find(script,null);
    }

    public Object insert(String script) throws Exception {
        script = parseSql(script);
        return this.insert(script,null);
    }

    public Object remove(String script) throws Exception {
        script = parseSql(script);
        return this.remove(script,null);
    }

    public Long update(String script) throws Exception {
        script = parseSql(script);
        return this.update(script,null);
    }

}
