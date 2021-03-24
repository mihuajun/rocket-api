package com.github.alenfive.rocketapi.function;

import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.extend.ISQLInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作函数
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
public class DbFunction extends HashMap<String,DbFunction> implements IFunction {

    private DataSourceManager dataSourceManager;

    private ApiInfoContent apiInfoContent;

    private IApiPager apiPager;

    private UtilsFunction utilsFunction;

    private ISQLInterceptor sqlInterceptor;

    private String datasource;

    @Override
    public DbFunction get(Object key){
        DbFunction dbFunction = new DbFunction(this.dataSourceManager,this.apiInfoContent,this.apiPager,this.utilsFunction,this.sqlInterceptor);
        dbFunction.setDatasource(key.toString());
        return dbFunction;
    }

    public DbFunction(DataSourceManager dataSourceManager,ApiInfoContent apiInfoContent,IApiPager apiPager,UtilsFunction utilsFunction,ISQLInterceptor sqlInterceptor){
        this.dataSourceManager = dataSourceManager;
        this.apiInfoContent = apiInfoContent;
        this.apiPager = apiPager;
        this.utilsFunction = utilsFunction;
        this.sqlInterceptor = sqlInterceptor;
    }

    public void setDatasource(String datasource){
        this.datasource = datasource;
    }

    public String getDatasource(){
        return this.datasource;
    }

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

    @Deprecated
    public Long count(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        List<Map<String,Object>> list = find(script,dataSource,params);
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

    @Deprecated
    public Map<String,Object> findOne(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        List<Map<String,Object>> list = find(script,dataSource,params);
        if (list.size() == 0)return null;
        return list.get(0);
    }

    @Deprecated
    public List<Map<String,Object>> find(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(script));
        List<Map<String,Object>> result = null;
        try {
            result = dataSourceManager.find(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
            sqlInterceptor.after(sbScript.toString());
        }

        return result;
    }

    @Deprecated
    public Object insert(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(script));
        Object result = null;
        try {
            result = dataSourceManager.insert(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
            sqlInterceptor.after(sbScript.toString());
        }
        return result;
    }

    @Deprecated
    public Object remove(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(script));
        Object result =  null;
        try {
            result = dataSourceManager.remove(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
            sqlInterceptor.after(sbScript.toString());
        }
        return result;
    }

    @Deprecated
    public Long update(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(script));
        Long result =  null;
        try {
            result = dataSourceManager.update(sbScript,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params);
        }finally {
            if (apiInfoContent.getIsDebug()){
                apiInfoContent.putLog("generate script:  " + sbScript);
            }
            log.info("generate script:{}",sbScript);
            sqlInterceptor.after(sbScript.toString());
        }
        return result;
    }

    @Deprecated
    public Object pager(String script,String dataSource,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        Page page = Page.builder()
                .pageNo(Integer.valueOf(utilsFunction.val(apiPager.getPageNoVarName()).toString()))
                .pageSize(Integer.valueOf(utilsFunction.val(apiPager.getPageSizeVarName()).toString()))
                .build();
        String totalSql = dataSourceManager.buildCountScript(script,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params,apiPager,page);
        Long total = this.count(totalSql,dataSource,params);
        List<Map<String,Object>> data = null;
        if (total > 0){
            String pageSql = dataSourceManager.buildPageScript(script,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params,apiPager,page);
            data = this.find(pageSql,dataSource,params);
        }else{
            data = Collections.emptyList();
        }
        return apiPager.buildPager(total,data,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams());
    }

    /*重载 script*/
    public Object pager(String script) throws Exception {
        script = parseSql(script);
        return this.pager(script,datasource,null);
    }

    public Long count(String script) throws Exception {
        script = parseSql(script);
        return this.count(script,datasource,null);
    }

    public Map<String,Object> findOne(String script) throws Exception {
        script = parseSql(script);
        return this.findOne(script,datasource,null);
    }

    public List<Map<String,Object>> find(String script) throws Exception {
        script = parseSql(script);
        return this.find(script,datasource,null);
    }

    public Object insert(String script) throws Exception {
        script = parseSql(script);
        return this.insert(script,datasource,null);
    }

    public Object remove(String script) throws Exception {
        script = parseSql(script);
        return this.remove(script,datasource,null);
    }

    public Long update(String script) throws Exception {
        script = parseSql(script);
        return this.update(script,datasource,null);
    }


    /*重载 datasource*/
    @Deprecated
    public Object pager(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.pager(script,datasource,null);
    }

    @Deprecated
    public Long count(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.count(script,datasource,null);
    }

    @Deprecated
    public Map<String,Object> findOne(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.findOne(script,datasource,null);
    }

    @Deprecated
    public List<Map<String,Object>> find(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.find(script,datasource,null);
    }

    @Deprecated
    public Object insert(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.insert(script,datasource,null);
    }

    @Deprecated
    public Object remove(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.remove(script,datasource,null);
    }

    @Deprecated
    public Long update(String script,String datasource) throws Exception {
        script = parseSql(script);
        return this.update(script,datasource,null);
    }

    /*重载 params*/
    public Object pager(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.pager(script,datasource,params);
    }

    public Long count(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.count(script,datasource,params);
    }

    public Map<String,Object> findOne(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.findOne(script,datasource,params);
    }

    public List<Map<String,Object>> find(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.find(script,datasource,params);
    }

    public Object insert(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.insert(script,datasource,params);
    }

    public Object remove(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.remove(script,datasource,params);
    }

    public Long update(String script,Map<String,Object> params) throws Exception {
        script = parseSql(script);
        return this.update(script,datasource,params);
    }
}
