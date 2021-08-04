package com.github.alenfive.rocketapi.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.vo.Page;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiPager;
import com.github.alenfive.rocketapi.extend.ISQLInterceptor;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mongo数据库操作函数
 */
@SuppressWarnings("DuplicatedCode")
@Component
@Slf4j
public class MongoFunction implements IFunction{

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private IApiPager apiPager;

    @Autowired
    private UtilsFunction utilsFunction;

    @Autowired
    private ISQLInterceptor sqlInterceptor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScriptParseService parseService;

    @Override
    public String getVarName() {
        return "mongodb";
    }

    public Long count(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {
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

    public Map<String,Object> findOne(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {
        List<Map<String,Object>> list = find(script,dataSource,params);
        if (list.size() == 0)return null;
        return list.get(0);
    }


    public List<Map<String,Object>> find(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {
        Document document = new Document(script);

        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(document.toJson()));
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

    public Object insert(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {
        Document document = new Document(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(document.toJson()));
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

    public Object remove(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {
        Document document = new Document(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(document.toJson()));
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

    public Long update(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {
        Document document = new Document(script);
        StringBuilder sbScript = new StringBuilder(sqlInterceptor.before(document.toJson()));
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

    public Object pager(Map<String,Object> script,String dataSource,Map<String,Object> params) throws Exception {

        Integer pageNo = buildPagerNo();
        Integer pageSize = buildPagerSize();
        apiInfoContent.getEngineBindings().put(apiPager.getPageNoVarName(),pageNo);
        apiInfoContent.getEngineBindings().put(apiPager.getPageSizeVarName(),pageSize);
        apiInfoContent.getEngineBindings().put(apiPager.getIndexVarName(),apiPager.getIndexVarValue(pageSize,pageNo));

        Document document = new Document(script);
        Page page = Page.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        String totalSql = dataSourceManager.buildCountScript(document.toJson(),apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params,apiPager,page);
        Long total = this.count(objectMapper.readValue(totalSql,Map.class),dataSource,params);
        List<Map<String,Object>> data = null;
        if (total > 0){
            String pageSql = dataSourceManager.buildPageScript(document.toJson(),apiInfoContent.getApiInfo(),apiInfoContent.getApiParams(),dataSource,params,apiPager,page);
            data = this.find(objectMapper.readValue(pageSql,Map.class),dataSource,params);
        }else{
            data = Collections.emptyList();
        }
        return apiPager.buildPager(total,data,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams());
    }

    private Integer buildPagerNo() {
        Object value = parseService.buildContentScopeParamItem(null,apiPager.getPageNoVarName());
        if (StringUtils.isEmpty(value)){
            return apiPager.getPageNoDefaultValue();
        }
        return Integer.valueOf(value.toString());
    }

    private Integer buildPagerSize() {
        Object value = parseService.buildContentScopeParamItem(null,apiPager.getPageSizeVarName());
        if (StringUtils.isEmpty(value)){
            return apiPager.getPageSizeDefaultValue();
        }
        return Integer.valueOf(value.toString());
    }

    /*重载 script*/
    public Object pager(Map<String,Object> script) throws Exception {
        return this.pager(script,null,null);
    }

    public Long count(Map<String,Object> script) throws Exception {
        return this.count(script,null,null);
    }

    public Map<String,Object> findOne(Map<String,Object> script) throws Exception {
        return this.findOne(script,null,null);
    }

    public List<Map<String,Object>> find(Map<String,Object> script) throws Exception {
        return this.find(script,null,null);
    }

    public Object insert(Map<String,Object> script) throws Exception {
        return this.insert(script,null,null);
    }

    public Object remove(Map<String,Object> script) throws Exception {
        return this.remove(script,null,null);
    }

    public Long update(Map<String,Object> script) throws Exception {
        return this.update(script,null,null);
    }


    /*重载 datasource*/
    public Object pager(Map<String,Object> script,String datasource) throws Exception {
        return this.pager(script,datasource,null);
    }

    public Long count(Map<String,Object> script,String datasource) throws Exception {
        return this.count(script,datasource,null);
    }

    public Map<String,Object> findOne(Map<String,Object> script,String datasource) throws Exception {
        return this.findOne(script,datasource,null);
    }

    public List<Map<String,Object>> find(Map<String,Object> script,String datasource) throws Exception {
        return this.find(script,datasource,null);
    }

    public Object insert(Map<String,Object> script,String datasource) throws Exception {
        return this.insert(script,datasource,null);
    }

    public Object remove(Map<String,Object> script,String datasource) throws Exception {
        return this.remove(script,datasource,null);
    }

    public Long update(Map<String,Object> script,String datasource) throws Exception {
        return this.update(script,datasource,null);
    }

    /*重载 params*/
    public Object pager(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.pager(script,null,params);
    }

    public Long count(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.count(script,null,params);
    }

    public Map<String,Object> findOne(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.findOne(script,null,params);
    }

    public List<Map<String,Object>> find(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.find(script,null,params);
    }

    public Object insert(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.insert(script,null,params);
    }

    public Object remove(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.remove(script,null,params);
    }

    public Long update(Map<String,Object> script,Map<String,Object> params) throws Exception {
        return this.update(script,null,params);
    }
}
