package com.github.alenfive.rocketapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.entity.vo.RenameGroupReq;
import com.github.alenfive.rocketapi.extend.*;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import com.github.alenfive.rocketapi.utils.GenerateId;
import com.github.alenfive.rocketapi.utils.PackageUtils;
import com.github.alenfive.rocketapi.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 将存储的API注册为request mapping,并且提供对入参及存储的执行脚本进行解析。
 * 输出解析后的最终脚本提供给脚本执行器`@Link DataSourceDialect`。然后对结果进行封装返回
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
@Component
public class QLRequestMappingFactory {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private ScriptParseService parseService;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Value("${spring.application.name}")
    private String service;


    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private IScriptParse scriptParse;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private RocketApiProperties properties;

    public List<ApiInfoInterceptor> interceptors = null;

    @Autowired
    private IApiInfoCache apiInfoCache;

    @Autowired
    private IResultWrapper resultWrapper;

    @Autowired
    private IScriptEncrypt scriptEncrypt;

    @Autowired
    private ServerProperties serverProperties;

    /**
     * 初始化db mapping
     */
    @PostConstruct
    public void init() throws Exception {

        //load banner
        loadBanner();

        //加载数据库API
        ApiParams apiParams = new ApiParams().putParam("service",service);
        StringBuilder script = new StringBuilder(dataSourceManager.listApiInfoScript());
        parseService.buildParams(script,apiParams);
        List<Map<String,Object>> apiInfos = dataSourceManager.find(script, ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
        for (Map<String,Object> map : apiInfos){
            ApiInfo apiInfo = objectMapper.readValue(objectMapper.writeValueAsBytes(map),ApiInfo.class);
            apiInfoCache.put(apiInfo);
        }

        //加载代码方式的API
        List<ApiInfo> codeApiList = getPathListForCode();
        for (ApiInfo codeInfo : codeApiList){
            ApiInfo dbInfo = apiInfoCache.get(codeInfo);
            if (dbInfo != null){
                continue;
            }
            codeInfo.setId(GenerateId.get().toHexString());
            codeInfo.setCreateTime(new Date());
            codeInfo.setUpdateTime(new Date());
            apiParams = ApiParams.builder().param(codeInfo.toMap()).build();
            script = new StringBuilder(dataSourceManager.saveApiInfoScript());
            parseService.buildParams(script,apiParams);
            dataSourceManager.insert(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),apiParams);
            apiInfoCache.put(getDbInfo(codeInfo));
        }

        //注册mapping
        for (ApiInfo apiInfo : apiInfoCache.getAll()){
            this.registerMappingForApiInfo(apiInfo);
        }
    }

    private void loadBanner() {
        System.out.println("__________               __           __       _____ __________.___ \n" +
                "\\______   \\ ____   ____ |  | __ _____/  |_    /  _  \\\\______   \\   |\n" +
                " |       _//  _ \\_/ ___\\|  |/ // __ \\   __\\  /  /_\\  \\|     ___/   |\n" +
                " |    |   (  <_> )  \\___|    <\\  ___/|  |   /    |    \\    |   |   |\n" +
                " |____|_  /\\____/ \\___  >__|_ \\\\___  >__|   \\____|__  /____|   |___|\n" +
                "        \\/            \\/     \\/    \\/               \\/              \n" +
                "\033[32;2m"+":: Rocket API ::"+"\033[m"+"        ("+ PackageUtils.getVersion()+")   " +buildLocalLink());

    }

    private String buildLocalLink(){
        String content = serverProperties.getServlet().getContextPath() == null?"":serverProperties.getServlet().getContextPath();
        return "http://localhost:"+serverProperties.getPort() + ("/"+content+ properties.getBaseRegisterPath()).replace("//","/");
    }

    private void clear(){
        apiInfoCache.getAll().stream().filter(item->ApiType.Ql.name().equals(item.getType())).forEach(item->{
            this.unregisterMappingForApiInfo(item);
        });
        apiInfoCache.removeAll();
    }





    /**
     * 执行脚本逻辑
     */
    @ResponseBody
    @RequestMapping
    public Object execute(@PathVariable(required = false) Map<String,String> pathVar,
                          @RequestParam(required = false) Map<String,Object> param,
                          HttpServletRequest request) throws Throwable {

        String path = buildPattern(request);
        String method = request.getMethod();
        Map<String,Object> body = new HashMap<>();


        if (request.getContentType() != null && request.getContentType().indexOf("application/json") > -1){
            try {
                body.putAll(objectMapper.readValue(request.getInputStream(),Map.class));
            }catch (MismatchedInputException exception){
                throw new HttpMessageNotReadableException("Required request body is missing",exception,new ServletServerHttpRequest(request));
            }
        }else if(request.getContentType() != null && request.getContentType().indexOf("multipart/form-data") > -1){
            MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
            body.putAll(multipartHttpServletRequest.getMultiFileMap());
        }

        ApiParams apiParams = ApiParams.builder()
                .pathVar(pathVar)
                .header(RequestUtils.buildHeaderParams(request))
                .param(param)
                .body(body)
                .session(RequestUtils.buildSessionParams(request))
                .request(request)
                .build();


        ApiInfo apiInfo = apiInfoCache.get(ApiInfo.builder().method(method).path(path).build());

        StringBuilder script = new StringBuilder(scriptEncrypt.decrypt(apiInfo.getScript()));

        try {
            Object data = scriptParse.runScript(script.toString(),apiInfo,apiParams);
            if (data instanceof IgnoreWrapper){
                return ((IgnoreWrapper)data).getData();
            }
            return resultWrapper.wrapper("0","succeeded",data,request,response);
        }catch (Exception e){
            e.printStackTrace();
            return resultWrapper.wrapper("500",e.getMessage(),null,request,response);
        }finally {
            apiInfoContent.removeAll();
        }
    }



    public String buildPattern(HttpServletRequest request) {
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
    private void registerMappingForApiInfo(ApiInfo apiInfo) throws NoSuchMethodException {
        if (ApiType.Code.name().equals(apiInfo.getType())){
            return;
        }
        if (StringUtils.isEmpty(apiInfo.getPath()) || apiInfo.getPath().startsWith("TEMP-")){
            return;
        }
        String pattern = apiInfo.getPath().replaceAll("/+","/");
        log.debug("register mapping [{}]{}",apiInfo.getMethod(),pattern);
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(pattern);
        RequestMethodsRequestCondition methodsRequestCondition = new RequestMethodsRequestCondition(RequestMethod.valueOf(apiInfo.getMethod()));
        RequestMappingInfo mappingInfo = new RequestMappingInfo(patternsRequestCondition,methodsRequestCondition,null,null,null,null,null);
        Method targetMethod = QLRequestMappingFactory.class.getDeclaredMethod("execute", Map.class, Map.class,HttpServletRequest.class);
        requestMappingHandlerMapping.registerMapping(mappingInfo,this, targetMethod);
    }

    /**
     * 取消注册mapping
     * @param apiInfo
     */
    private void unregisterMappingForApiInfo(ApiInfo apiInfo){
        if (ApiType.Code.name().equals(apiInfo.getType())){
            return;
        }
        if (StringUtils.isEmpty(apiInfo.getPath()) || apiInfo.getPath().startsWith("TEMP-")){
            return;
        }
        log.debug("unregister mapping [{}]{}",apiInfo.getMethod(),apiInfo.getPath());
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(apiInfo.getPath());
        RequestMethodsRequestCondition methodsRequestCondition = new RequestMethodsRequestCondition(RequestMethod.valueOf(apiInfo.getMethod()));
        RequestMappingInfo mappingInfo = new RequestMappingInfo(patternsRequestCondition,methodsRequestCondition,null,null,null,null,null);
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);
    }

    public Collection<ApiInfo> getPathList(boolean isDb) throws Exception {
        if (isDb){
            clear();
            init();
        }
        return apiInfoCache.getAll().stream().sorted(Comparator.comparing(ApiInfo::getComment).thenComparing(ApiInfo::getPath)).collect(Collectors.toList());
    }

    @Transactional
    public String saveOrUpdateApiInfo(ApiInfo apiInfo) throws Exception {

        if (existsPattern(apiInfo)){
            throw new IllegalArgumentException("method: "+apiInfo.getMethod()+" path:"+apiInfo.getPath()+" already exist");
        }

        apiInfo.setUpdateTime(new Date());
        if (apiInfo.getId() == null){
            apiInfo.setType(ApiType.Ql.name());
            apiInfo.setCreateTime(new Date());
            apiInfo.setService(service);
            apiInfo.setId(GenerateId.get().toHexString());
            ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
            StringBuilder script = new StringBuilder(dataSourceManager.saveApiInfoScript());
            parseService.buildParams(script,apiParams);
            dataSourceManager.insert(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
        }else{
            apiInfo.setService(service);
            ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
            StringBuilder script = new StringBuilder(dataSourceManager.updateApiInfoScript());
            parseService.buildParams(script,apiParams);
            dataSourceManager.update(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);

            ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);

            //取消mapping注册
            unregisterMappingForApiInfo(dbInfo);

            //清理缓存
            apiInfoCache.remove(dbInfo);
        }

        ApiInfo dbInfo = getDbInfo(apiInfo);

        //入缓存
        apiInfoCache.put(dbInfo);

        //注册mapping
        this.registerMappingForApiInfo(dbInfo);

        //存储历史
        saveApiHistory(dbInfo);

        return dbInfo.getId();
    }

    private void saveApiHistory(ApiInfo dbInfo) throws Exception {
        ApiInfoHistory history = new ApiInfoHistory();
        BeanUtils.copyProperties(dbInfo,history);
        history.setApiInfoId(dbInfo.getId());
        history.setId(GenerateId.get().toString());
        history.setCreateTime(new Date());
        ApiParams apiParams = ApiParams.builder().param(history.toMap()).build();
        StringBuilder script = new StringBuilder(dataSourceManager.saveApiInfoHistoryScript());
        parseService.buildParams(script,apiParams);
        dataSourceManager.insert(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
    }

    public ApiInfo getDbInfo(ApiInfo apiInfo) throws Exception {
        ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
        StringBuilder script = new StringBuilder(dataSourceManager.getApiInfoScript());
        parseService.buildParams(script,apiParams);

        List<Map<String,Object>> apiInfoMap = dataSourceManager.find(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
        return objectMapper.readValue(objectMapper.writeValueAsBytes(apiInfoMap.get(0)),ApiInfo.class);
    }

    private boolean existsPattern(ApiInfo apiInfo) {
        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getPath().equals(apiInfo.getPath()) && (item.getMethod().equals("All") || item.getMethod().equals(apiInfo.getMethod()))).findFirst().orElse(null);
        if (dbInfo == null || (apiInfo.getId() != null && apiInfo.getId().equals(dbInfo.getId()))){
            return false;
        }
        return true;
    }

    @Transactional
    public Long deleteApiInfo(ApiInfo apiInfo) throws Exception {

        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
        if (dbInfo == null){
            return 0L;
        }

        ApiParams apiParams = ApiParams.builder()
                .param(apiInfo.toMap())
                .build();

        //清数据库
        StringBuilder script = new StringBuilder(dataSourceManager.deleteApiInfoScript());
        parseService.buildParams(script,apiParams);
        Long num = dataSourceManager.remove(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);

        //清缓存
        apiInfoCache.remove(dbInfo);

        //取消mapping注册
        unregisterMappingForApiInfo(dbInfo);
        return num;
    }

    /**
     * 获取已注册的API地址
     */
    public List<ApiInfo> getPathListForCode(){
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        List<ApiInfo> result = new ArrayList<>(map.size());
        for (RequestMappingInfo info : map.keySet()) {
            String group = map.get(info).getBeanType().getSimpleName();
            for(String path : info.getPatternsCondition().getPatterns()){

                //过滤本身的类
                if (path.indexOf(properties.getBaseRegisterPath()) == 0 || path.equals("/error")){
                    continue;
                }

                Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
                if (methods.isEmpty()){
                    result.add(ApiInfo.builder()
                            .path(path)
                            .method("All")
                            .type(ApiType.Code.name())
                            .service(service)
                            .group(group)
                            .editor("admin")
                            .comment("")
                            .datasource("")
                            .script("")
                            .options("")
                            .build());
                }else{
                    for (RequestMethod method : methods){
                        result.add(ApiInfo.builder()
                                .path(path)
                                .method(method.name())
                                .type(ApiType.Code.name())
                                .service(service)
                                .group(group)
                                .editor("admin")
                                .comment("")
                                .datasource("")
                                .script("")
                                .options("")
                                .build());
                    }
                }

            }
        }
        return result;
    }

    public Set<String> getGroupNameList() {
        return apiInfoCache.getAll().stream().map(ApiInfo::getGroup).collect(Collectors.toSet());
    }

    public Set<String> getApiNameList(String group) {
        return apiInfoCache.getAll().stream().filter(item->group.equals(item.getGroup()))
                .map(item->StringUtils.isEmpty(item.getComment())?item.getPath():item.getComment()).collect(Collectors.toSet());
    }

    @Transactional
    public Long renameGroup(RenameGroupReq renameGroupReq) throws Exception {
        List<ApiInfo> apiInfos = apiInfoCache.getAll().stream().filter(item->item.getGroup().equals(renameGroupReq.getOldGroup())).collect(Collectors.toList());
        for (ApiInfo apiInfo : apiInfos){
            apiInfo.setGroup(renameGroupReq.getNewGroup());
            StringBuilder script = new StringBuilder(dataSourceManager.updateApiInfoScript());
            parseService.buildParams(script,ApiParams.builder().param(apiInfo.toMap()).build());
            dataSourceManager.update(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);

            //更新缓存
            apiInfoCache.put(apiInfo);
        }
        return Long.valueOf(apiInfos.size());
    }

    @Transactional
    public Object saveExample(ApiExample apiExample) throws Exception {
        StringBuilder script = new StringBuilder(dataSourceManager.saveApiExampleScript());
        parseService.buildParams(script,ApiParams.builder().param(apiExample.toMap()).build());
        return dataSourceManager.insert(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
    }

    public List<Map<String,Object>> lastApiExample(String apiInfoId, Integer limit) throws Exception {
        ApiParams apiParams = new ApiParams();
        apiParams.putParam("apiInfoId",apiInfoId);
        apiParams.putParam("limit",limit);
        StringBuilder script = new StringBuilder(dataSourceManager.lastApiExampleScript());
        parseService.buildParams(script,apiParams);
        return dataSourceManager.find(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
    }

    @Transactional
    public Long deleteExampleList(ArrayList<ApiExample> apiExampleList) throws Exception {
        StringBuilder script = new StringBuilder(dataSourceManager.deleteExampleScript());
        parseService.buildParams(script,new ApiParams().putParam("ids",apiExampleList.stream().map(ApiExample::getId).collect(Collectors.toSet())));
        return dataSourceManager.remove(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
    }

    public void addInterceptor(ApiInfoInterceptor interceptor){
        if (this.interceptors == null){
            this.interceptors = new ArrayList<>();
        }
        this.interceptors.add(interceptor);
    }

    public List<ApiInfoHistory> lastApiInfo(String apiInfoId,Integer index, Integer pageSize) throws Exception {
        StringBuilder script = new StringBuilder(dataSourceManager.lastApiInfoHistoryScript());
        ApiParams apiParams = new ApiParams();
        apiParams.putParam("apiInfoId",apiInfoId);
        apiParams.putParam("pageSize",pageSize);
        apiParams.putParam("index",index);
        apiParams.putParam("service",service);
        parseService.parse(script,apiParams);
        return dataSourceManager.find(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null)
                .stream().map(item-> {
            try {
                return objectMapper.readValue(objectMapper.writeValueAsBytes(item),ApiInfoHistory.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void apiInfoSync(List<ApiInfo> apiInfos,Boolean increment) throws Exception {

        Collection<ApiInfo> currApiInfos = this.getPathList(false);

        //全量同步
        if(!increment){

            //删除历史信息
            for (ApiInfo dbInfo : currApiInfos){
                ApiParams apiParams = ApiParams.builder().param(dbInfo.toMap()).build();
                StringBuilder script = new StringBuilder(dataSourceManager.deleteApiInfoScript());
                parseService.buildParams(script,apiParams);
                dataSourceManager.remove(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
            }
            //添加新信息
            for (ApiInfo apiInfo : apiInfos){
                apiInfo.setCreateTime(new Date());
                apiInfo.setUpdateTime(new Date());
                ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
                StringBuilder script = new StringBuilder(dataSourceManager.saveApiInfoScript());
                parseService.buildParams(script,apiParams);
                dataSourceManager.insert(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }else {
            //增量同步
            for (ApiInfo apiInfo : apiInfos){
                ApiInfo dbInfo =  currApiInfos.stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
                if (dbInfo == null){
                    apiInfo.setCreateTime(new Date());
                    apiInfo.setUpdateTime(new Date());
                    ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
                    StringBuilder script = new StringBuilder(dataSourceManager.saveApiInfoScript());
                    parseService.buildParams(script,apiParams);
                    dataSourceManager.insert(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
                }else{
                    apiInfo.setUpdateTime(new Date());
                    ApiParams apiParams = ApiParams.builder().param(apiInfo.toMap()).build();
                    StringBuilder script = new StringBuilder(dataSourceManager.updateApiInfoScript());
                    parseService.buildParams(script,apiParams);
                    dataSourceManager.update(script,ApiInfo.builder().datasource(dataSourceManager.getStoreApiKey()).build(),null);
                }

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }

        //刷新缓存
        this.getPathList(true);
    }
}
