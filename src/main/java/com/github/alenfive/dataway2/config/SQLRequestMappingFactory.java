package com.github.alenfive.dataway2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.dataway2.entity.*;
import com.github.alenfive.dataway2.extend.ApiPagerInterface;
import com.github.alenfive.dataway2.extend.DataSourceDialect;
import com.github.alenfive.dataway2.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/5/27 16:30
 * @UpdateDate: 2020/5/27 16:30
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu SQL mapping 注册
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
@Component
public class SQLRequestMappingFactory {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ScriptParseService parseService;

    private final List<String> blankList = Arrays.asList(
            "/dataway2",
            "/error",
            "/api-ui"
    );

    @Autowired
    private ApplicationContext appContext;

    @Autowired
    private DataSourceDialect dataSourceDialect;

    @Autowired
    private ApiPagerInterface apiPager;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private Dataway2Properties properties;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, ApiInfo> cacheApiInfo = new ConcurrentHashMap<>();

    /**
     * 初始化db mapping
     */
    @PostConstruct
    public void init() throws IOException {



        //加载数据库API
        List<Map<String,Object>> apiInfos = dataSourceDialect.executeQuery(dataSourceDialect.listApiInfoScript(),null,null);
        for (Map<String,Object> map : apiInfos){
            ApiInfo apiInfo = objectMapper.readValue(objectMapper.writeValueAsBytes(map),ApiInfo.class);
            this.cacheApiInfo.put(buildApiInfoKey(apiInfo),apiInfo);
        }

        //加载代码方式的API
        List<ApiInfo> codeApiList = getPathListForCode();
        for (ApiInfo codeInfo : codeApiList){
            ApiInfo dbInfo = this.cacheApiInfo.get(buildApiInfoKey(codeInfo));
            if (dbInfo != null){
                continue;
            }

            codeInfo.setCreateTime(new Date());
            codeInfo.setUpdateTime(new Date());
            ApiParams apiParams = ApiParams.builder().param(codeInfo.toMap()).build();
            StringBuilder script = new StringBuilder(dataSourceDialect.saveApiInfoScript());
            parseService.buildParams(script,apiParams);
            dataSourceDialect.execute(script.toString(),null,null);
            this.cacheApiInfo.put(buildApiInfoKey(codeInfo),codeInfo);
        }

        //注册mapping
        for (ApiInfo apiInfo : this.cacheApiInfo.values()){
            registerMappingForApiInfo(apiInfo);
        }

    }

    private String buildApiInfoKey(ApiInfo apiInfo) {
        return apiInfo.getMethod() + apiInfo.getPath();
    }



    /**
     * 执行脚本逻辑
     * @return
     */
    @ResponseBody
    public Object execute(@PathVariable(required = false) Map<String,Object> pathVar,
                          @RequestParam(required = false) Map<String,Object> param,
                          @RequestBody(required = false) Map<String,Object> body) {

        String path = buildPath(request);
        String method = request.getMethod();
        ApiParams apiParams = ApiParams.builder()
                .pathVar(pathVar)
                .param(param)
                .body(body)
                .request(request)
                .build();
        ApiInfo apiInfo = cacheApiInfo.get(buildApiInfoKey(ApiInfo.builder().method(method).path(path).build()));

        //提取脚本
        List<StringBuilder> scriptList = parseService.extractExecutableScript(apiInfo.getScript());

        //脚本解析
        scriptList.forEach(item->{
            parseService.parse(item,apiParams);
            log.debug("generate sql:{}",item.toString());
        });

        return buildResult(scriptList,apiInfo,apiParams);

    }

    private Object buildResult(List<StringBuilder> scriptList,ApiInfo apiInfo, ApiParams apiParams){

        String reaultType = apiInfo.getPath().substring(apiInfo.getPath().lastIndexOf("/")+1);

        if (ApiResultType.first.name().equals(reaultType)){
            List<Map<String,Object>> resultList = dataSourceDialect.executeQuery(scriptList.get(0).toString(),apiInfo,apiParams);
            return resultList.size()==0?Collections.EMPTY_MAP:resultList.get(0);
        }

        if (ApiResultType.list.name().equals(reaultType)){
            return dataSourceDialect.executeQuery(scriptList.get(0).toString(),apiInfo,apiParams);
        }

        if (ApiResultType.page.name().equals(reaultType)){

            Integer pageNo = getPagerNo(apiParams);
            Integer pageSize = getPagerSize(apiParams);
            apiParams.putParam(apiPager.getIndexVarName(),(pageNo-1)*pageSize);

            Long totalRecords = dataSourceDialect.executeCount(scriptList.get(0).toString(),apiInfo,apiParams);
            List<Map<String,Object>> resultList = dataSourceDialect.executeQuery(scriptList.get(1).toString(),apiInfo,apiParams);
            return apiPager.buildPager(totalRecords,resultList,apiInfo,apiParams);
        }

        for (StringBuilder script : scriptList){
            dataSourceDialect.execute(script.toString(),apiInfo,apiParams);
        }
        return null;
    }

    private Integer getPagerNo(ApiParams apiParams) {
        String value = parseService.buildParamItem(apiParams,apiPager.getPageNoVarName());
        return StringUtils.isEmpty(value)?apiPager.getPageNoDefaultValue():Integer.valueOf(value);
    }

    private Integer getPagerSize(ApiParams apiParams) {
        String value = parseService.buildParamItem(apiParams,apiPager.getPageSizeVarName());
        return StringUtils.isEmpty(value)?apiPager.getPageSizeDefaultValue():Integer.valueOf(value);
    }

    private String buildPath(HttpServletRequest request){
        String pattern = buildPattern(request);
        return pattern.replaceFirst(properties.getApiPrefix(),"");
    }

    private String buildPattern(HttpServletRequest request) {
        Set<RequestMappingInfo> infos = requestMappingHandlerMapping.getHandlerMethods().keySet();
        RequestMappingInfo currInfo = null;
        for (RequestMappingInfo info : infos){
            if ((currInfo = info.getMatchingCondition(request)) != null){
                break;
            }
        }
        return currInfo.getPatternsCondition().getPatterns().iterator().next();
    }

    /**
     * 注册mapping
     * @param apiInfo
     */
    private void registerMappingForApiInfo(ApiInfo apiInfo){
        if (ApiType.Code.equals(apiInfo.getType())){
            return;
        }
        String pattern = properties.getApiPrefix()+apiInfo.getPath();
        log.debug("register mapping [{}]{}",apiInfo.getMethod(),pattern);
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(pattern);
        RequestMethodsRequestCondition methodsRequestCondition = new RequestMethodsRequestCondition(RequestMethod.valueOf(apiInfo.getMethod()));
        RequestMappingInfo mappingInfo = new RequestMappingInfo(patternsRequestCondition,methodsRequestCondition,null,null,null,null,null);
        Method targetMethod = ReflectionUtils.findMethod(this.getClass(), "execute",Map.class,Map.class,Map.class);
        requestMappingHandlerMapping.registerMapping(mappingInfo,this.getClass().getSimpleName(), targetMethod);
    }

    /**
     * 取消注册mapping
     * @param apiInfo
     */
    private void unregisterMappingForApiInfo(ApiInfo apiInfo){
        if (ApiType.Code.equals(apiInfo.getType())){
            return;
        }
        String pattern = properties.getApiPrefix()+apiInfo.getPath();
        log.debug("unregister mapping [{}]{}",apiInfo.getMethod(),pattern);
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(pattern);
        RequestMethodsRequestCondition methodsRequestCondition = new RequestMethodsRequestCondition(RequestMethod.valueOf(apiInfo.getMethod()));
        RequestMappingInfo mappingInfo = new RequestMappingInfo(patternsRequestCondition,methodsRequestCondition,null,null,null,null,null);
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);
    }

    public Collection<ApiInfo> getPathList(){
        return this.cacheApiInfo.values();
    }

    public void saveOrUpdateApiInfo(ApiInfo apiInfo) throws IOException {
        apiInfo.setUpdateTime(new Date());
        if (apiInfo.getId() == null){

            ApiInfo dbInfo = this.cacheApiInfo.get(buildApiInfoKey(apiInfo));
            if (dbInfo != null){
                throw new IllegalArgumentException(buildApiInfoKey(apiInfo)+"already exist");
            }

            apiInfo.setCreateTime(new Date());

            ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
            StringBuilder script = new StringBuilder(dataSourceDialect.saveApiInfoScript());
            parseService.buildParams(script,apiParams);
            dataSourceDialect.execute(script.toString(),null,null);
        }else{
            ApiInfo dbInfo = this.cacheApiInfo.values().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);

            ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
            StringBuilder script = new StringBuilder(dataSourceDialect.updateApiInfoScript());
            parseService.buildParams(script,apiParams);
            dataSourceDialect.execute(script.toString(),null,null);

            //取消mapping注册
            unregisterMappingForApiInfo(dbInfo);

            //清理缓存
            this.cacheApiInfo.remove(buildApiInfoKey(dbInfo));
        }

        ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
        StringBuilder script = new StringBuilder(dataSourceDialect.getApiInfoScript());
        parseService.buildParams(script,apiParams);

        List<Map<String,Object>> apiInfoMap = dataSourceDialect.executeQuery(script.toString(),null,null);
        ApiInfo dbInfo = objectMapper.readValue(objectMapper.writeValueAsBytes(apiInfoMap.get(0)),ApiInfo.class);

        //入缓存
        this.cacheApiInfo.put(buildApiInfoKey(dbInfo),dbInfo);

        //注册mapping
        this.registerMappingForApiInfo(dbInfo);
    }

    public void deleteApiInfo(ApiInfo apiInfo) {

        ApiInfo dbInfo = this.cacheApiInfo.values().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
        if (dbInfo == null){
            return;
        }

        ApiParams apiParams = ApiParams.builder()
                .param(apiInfo.toMap())
                .build();

        //清数据库
        StringBuilder script = new StringBuilder(dataSourceDialect.deleteApiInfoScript());
        parseService.buildParams(script,apiParams);
        dataSourceDialect.execute(script.toString(),null,null);

        //清缓存
        this.cacheApiInfo.remove(buildApiInfoKey(dbInfo));

        //取消mapping注册
        unregisterMappingForApiInfo(dbInfo);
    }

    /**
     * 获取已注册的API地址
     * @return
     */
    public List<ApiInfo> getPathListForCode(){
        RequestMappingHandlerMapping mapping = appContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        List<ApiInfo> result = new ArrayList<>(map.size());
        for (RequestMappingInfo info : map.keySet()) {
            for(String path : info.getPatternsCondition().getPatterns()){

                String blankPath = blankList.stream().filter(item->path.startsWith(item)).findFirst().orElse(null);
                if (!StringUtils.isEmpty(blankPath)){
                    continue;
                }

                Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
                if (methods.isEmpty()){
                    result.add(ApiInfo.builder()
                            .path(path)
                            .method("All")
                            .type(ApiType.Code.name())
                            .group("公共API")
                            .editor("admin")
                            .comment("")
                            .script("")
                            .params("")
                            .build());
                }else{
                    for (RequestMethod method : methods){
                        result.add(ApiInfo.builder()
                                .path(path)
                                .method(method.name())
                                .type(ApiType.Code.name())
                                .group("公共API")
                                .editor("admin")
                                .comment("")
                                .script("")
                                .params("")
                                .build());
                    }
                }

            }
        }
        return result;
    }
}
