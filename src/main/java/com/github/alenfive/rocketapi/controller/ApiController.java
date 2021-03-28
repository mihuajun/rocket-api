package com.github.alenfive.rocketapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.*;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiDocSync;
import com.github.alenfive.rocketapi.extend.IScriptEncrypt;
import com.github.alenfive.rocketapi.extend.IUserAuthorization;
import com.github.alenfive.rocketapi.function.IFunction;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.LoginService;
import com.github.alenfive.rocketapi.utils.GenerateId;
import com.github.alenfive.rocketapi.utils.PackageUtil;
import com.github.alenfive.rocketapi.utils.RequestUtils;
import com.github.alenfive.rocketapi.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Api ui 数据接口
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
@RestController
@RequestMapping("${spring.rocket-api.base-register-path:/interface-ui}")
@ConditionalOnProperty(name = "spring.rocket-api.view-enabled",havingValue = "true",matchIfMissing = true)
public class ApiController {

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private IScriptParse scriptParse;

    @Autowired
    private IUserAuthorization userAuthorization;

    @Autowired
    private LoginService loginService;

    @Autowired
    private IScriptEncrypt scriptEncrypt;

    @Autowired
    private IApiDocSync apiDocSync;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private Map<String,Object> cache = new ConcurrentHashMap<>();

    /**
     * LOAD API LIST
     */
    @GetMapping("/api-list")
    public ApiResult getPathList(boolean isDb) throws Exception {
        List<ApiInfo> result = mappingFactory.getPathList(isDb).stream()
                .sorted(Comparator.comparing(ApiInfo::getName).thenComparing(ApiInfo::getFullPath))
                .collect(Collectors.toList());


        result = result.stream().map(item->{
            ApiInfo apiInfo = new ApiInfo();
            BeanUtils.copyProperties(item,apiInfo);
            return apiInfo;
        }).collect(Collectors.toList());

        return  ApiResult.success(result);
    }

    /**
     * 单个获取
     */
    @GetMapping("/api-info/{id}")
    public ApiResult getPathList(@PathVariable String id) throws Exception {
        ApiInfo apiInfo = mappingFactory.getPathList(false).stream().filter(item->item.getId().equals(id)).findFirst().orElse(null);

        if (apiInfo == null || StringUtils.isEmpty(apiInfo.getScript())){
            return ApiResult.success(apiInfo);
        }

        ApiInfo resultInfo = new ApiInfo();
        BeanUtils.copyProperties(apiInfo,resultInfo);
        resultInfo.setScript(scriptEncrypt.decrypt(resultInfo.getScript()));
        return ApiResult.success(resultInfo);
    }

    /**
     * 历史记录查询
     */
    @GetMapping("/api-info/last")
    public ApiResult lastApiInfo(String apiInfoId,Integer pageSize,Integer pageNo) throws Exception {
        if (StringUtils.isEmpty(apiInfoId)){
            return ApiResult.success(null);
        }
        List<ApiInfoHistory> historyList = mappingFactory.lastApiInfo(apiInfoId,pageSize,pageNo);
        for (ApiInfoHistory history : historyList) {
            history.setScript(scriptEncrypt.decrypt(history.getScript()));
        }
        return  ApiResult.success(historyList);
    }

    /**
     * SAVE APIINFO
     * @param apiInfo
     */
    @PostMapping("/api-info")
    public ApiResult saveOrUpdateApiInfo(@RequestBody ApiInfo apiInfo,HttpServletRequest request) {

        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        apiInfo.setEditor(user);
        try {
            if (!StringUtils.isEmpty(apiInfo.getScript())){
                apiInfo.setScript(scriptEncrypt.encrypt(apiInfo.getScript()));
            }
            return ApiResult.success(mappingFactory.saveApiInfo(apiInfo));
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }

    }

    /**
     * 向远程服务进行同步
     */
    @PostMapping("/remote-sync")
    public Object apiInfoRemoteSync(@RequestBody RemoteApiInfoSyncReq syncReq,HttpServletRequest request) throws Exception {

        if (syncReq == null
                || StringUtils.isEmpty(syncReq.getRemoteUrl())
                || StringUtils.isEmpty(syncReq.getSecretKey())
                || syncReq.getIncrement() == null){
            return ApiResult.fail("Parameter is missing");
        }

        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        Collection<ApiInfo> apiInfos = null;
        if (syncReq.getIncrement() == 1){
            apiInfos = mappingFactory.getPathList(false).stream().filter(item->syncReq.getApiInfoIds().contains(item.getId())).collect(Collectors.toList());
        }else{
            apiInfos = mappingFactory.getPathList(false);
        }
        try {
            //签名验证
            Map<String,Object> signMap = new HashMap<>(4);
            signMap.put("timestamp",System.currentTimeMillis());
            signMap.put("increment",syncReq.getIncrement());
            signMap.put("apiInfos",objectMapper.writeValueAsString(apiInfos));
            String sign = SignUtils.build(syncReq.getSecretKey(),signMap);
            signMap.put("apiInfos",apiInfos);
            signMap.put("sign",sign);

            String remoteUrl = syncReq.getRemoteUrl().endsWith("/")?syncReq.getRemoteUrl().substring(0,syncReq.getRemoteUrl().length()-1):syncReq.getRemoteUrl();
            String url = remoteUrl+(rocketApiProperties.getBaseRegisterPath()+"/accept-sync").replace("//","/");
            SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(60000);
            factory.setReadTimeout(60000);
            RestTemplate restTemplate = new RestTemplate(factory);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> requestHttpEntity = new HttpEntity<>(objectMapper.writeValueAsString(signMap), headers);
            ResponseEntity<Object> postForEntity = restTemplate.postForEntity(url, requestHttpEntity, Object.class);
            return postForEntity.getBody();
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.toString());
        }
    }

    /**
     * REMOVE APIINFO
     * @param apiInfo
     */
    @DeleteMapping("/api-info")
    public ApiResult deleteApiInfo(@RequestBody ApiInfo apiInfo,HttpServletRequest request){
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            mappingFactory.deleteApiInfo(apiInfo);
            return ApiResult.success(null);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }

    }

    /**
     * 脚本执行
     */
    @PostMapping("/api-info/run")
    public ApiResult runScript(@RequestBody RunApiReq runApiReq, HttpServletRequest request){

        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        RunApiRes runApiRes = new RunApiRes();
        try {
            apiInfoContent.setIsDebug(runApiReq.isDebug());
            ApiInfo apiInfo = ApiInfo.builder()
                    .fullPath(runApiReq.getPattern())
                    .options(runApiReq.getOptions())
                    .datasource(runApiReq.getDatasource())
                    .script(runApiReq.getScript())
                    .build();
            ApiParams apiParams = ApiParams.builder()
                    .header(decodeHeaderValue(runApiReq.getHeader()))
                    .pathVar(getPathVar(runApiReq.getPattern(),runApiReq.getUrl()))
                    .param(getParam(runApiReq.getUrl()))
                    .body(buildBody(runApiReq.getBody()))
                    .session(RequestUtils.buildSessionParams(request))
                    .build();
            Object value = scriptParse.runScript(apiInfo.getScript(),apiInfo,apiParams);
            runApiRes.setData(value);
            return ApiResult.success(runApiRes);
        }catch (Throwable e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage(),runApiRes);
        }finally {
            runApiRes.setLogs(apiInfoContent.getLogs());
            apiInfoContent.removeAll();
        }
    }

    private Map<String, Object> buildBody(Object body) {
        Map<String,Object> params = new HashMap<>();
        if (body instanceof Map){
            params.putAll((Map<? extends String, ?>) body);
        }
        params.put(rocketApiProperties.getBodyRootKey(),body);
        return params;
    }


    private Map<String,String> decodeHeaderValue(Map<String,String> header) throws UnsupportedEncodingException {
        Map<String,String> newHeader = new HashMap<>(header.size());
        for (String key : header.keySet()){
            newHeader.put(key.toLowerCase(),URLDecoder.decode(header.get(key),"utf-8"));
        }
        return newHeader;
    }

    private Map<String,String> getPathVar(String pattern,String url){
        Integer beginIndex = url.indexOf("/",7);
        if (beginIndex == -1){
            return null;
        }
        Integer endIndex = url.indexOf("?") == -1?url.length():url.indexOf("?");
        String path = url.substring(beginIndex,endIndex);
        AntPathMatcher matcher = new AntPathMatcher();
        if (matcher.match(pattern,path)){
            return matcher.extractUriTemplateVariables(pattern,path);
        }
        return null;
    }

    private Map<String,Object> getParam(String url) {
        Map<String,Object> result = new HashMap<>();
        MultiValueMap<String, String> urlMvp = UriComponentsBuilder.fromHttpUrl(url).build().getQueryParams();
        urlMvp.forEach((key,value)->{
            String firstValue = CollectionUtils.isEmpty(value)?null:value.get(0);
            result.put(key,firstValue);
        });
        return  result;
    }

    /**
     * 模拟参数保存
     */
    @PostMapping("/api-example")
    public ApiResult saveExample(@RequestBody ApiExample apiExample,HttpServletRequest request) throws Exception {

        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        if (StringUtils.isEmpty(apiExample.getMethod())
                || StringUtils.isEmpty(apiExample.getUrl())
                || StringUtils.isEmpty(apiExample.getRequestHeader())){
            return ApiResult.fail("Send, then Save");
        }
        apiExample.setEditor(user);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        apiExample.setCreateTime(sdf.format(new Date()));
        apiExample.setId(GenerateId.get().toHexString());
        if (!StringUtils.isEmpty(apiExample.getResponseBody())){
            apiExample.setResponseBody(URLEncoder.encode(apiExample.getResponseBody(),"utf-8"));
        }

        return ApiResult.success(mappingFactory.saveExample(apiExample));
    }

    /**
     * 查询最近一次模拟数据
     */
    @GetMapping("/api-example/last")
    public ApiResult lastApiExample(String apiInfoId,Integer pageSize,Integer pageNo) throws Exception {

        List<ApiExample> result = mappingFactory.listApiExampleScript(apiInfoId,pageSize,pageNo);
        result.forEach(item->{
            if (!StringUtils.isEmpty(item.getResponseBody())){
                try {
                    item.setResponseBody(URLDecoder.decode(item.getResponseBody(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        return ApiResult.success(result);
    }

    /**
     * 删除模拟数据
     */
    @DeleteMapping("/api-example")
    private ApiResult deleteExampleList(@RequestBody DeleteExamleReq deleteExamleReq, HttpServletRequest request) throws Exception {
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }
        mappingFactory.deleteExampleList(deleteExamleReq.getApiExampleList());
        return ApiResult.success(null);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResult login(@RequestBody LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String user = userAuthorization.validate(loginVo.getUsername(), loginVo.getPassword());
        if (!StringUtils.isEmpty(user)){
            return ApiResult.success(loginService.getToken(loginVo));
        }

        return ApiResult.fail("Incorrect username or password");
    }

    /**
     * 注销登录
     */
    @PostMapping("/logout")
    public ApiResult login(HttpServletRequest request,HttpServletResponse response){
        return ApiResult.success(null);
    }

    /**
     * API DOC 同步
     */
    @GetMapping("/api-doc-push")
    public ApiResult apiDocPush(String apiInfoId) throws Exception {
        Collection<ApiInfo> apiInfos = mappingFactory.getPathList(false);
        String result = null;
        if (!StringUtils.isEmpty(apiInfoId)){
            ApiInfo apiInfo = apiInfos.stream().filter(item->item.getId().equals(apiInfoId)).findFirst().orElse(null);
            result = apiDocSync.sync(apiInfo,buildLastApiExample(apiInfo.getId()));
        }else{
            for(ApiInfo apiInfo : apiInfos){
                result = apiDocSync.sync(apiInfo,buildLastApiExample(apiInfo.getId()));
            }
        }
        return ApiResult.success(result);
    }

    private ApiExample buildLastApiExample(String apiInfoId) {
        List<ApiExample> result = mappingFactory.listApiExampleScript(apiInfoId,1,1);
        if (CollectionUtils.isEmpty(result)){
            return null;
        }
        ApiExample apiExample = result.get(0);
        try {
            if (!StringUtils.isEmpty(apiExample.getResponseBody())){
                apiExample.setResponseBody(URLDecoder.decode(apiExample.getResponseBody(),"utf-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return apiExample;
    }

    /**
     * 动态配置获取
     * @param request
     * @return
     */
    @GetMapping("/api-config")
    public ApiResult getApiConfig(HttpServletRequest request){
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        Object result = null;
        try {
            result = mappingFactory.getApiConfig();
        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
        return ApiResult.success(result);
    }

    /**
     * 动态配置修改
     * @param params
     * @return
     */
    @PostMapping("/api-config")
    public ApiResult saveApiConfig(@RequestBody(required = false) String configContext,HttpServletRequest request){
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            mappingFactory.saveApiConfig(configContext);
        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
        return ApiResult.success(null);
    }

    /**
     * 目录查询
     * @return
     */
    @GetMapping("/directory/list")
    public ApiResult directoryList(){
        return ApiResult.success(mappingFactory.loadDirectoryList(false).stream()
                .sorted(Comparator.comparing(ApiDirectory::getName).thenComparing(ApiDirectory::getPath))
                .collect(Collectors.toList()));
    }

    /**
     * 目录保存
     * @param directory
     * @return
     */
    @PostMapping("/directory")
    public ApiResult saveDirectory(@RequestBody ApiDirectory directory,HttpServletRequest request){
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            mappingFactory.saveDirectory(directory);
            mappingFactory.loadDirectoryList(true);
        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
        return ApiResult.success(directory.getId());
    }

    /**
     * 目录删除
     * @param directory
     * @return
     */
    @DeleteMapping("/directory")
    public ApiResult removeDirectory(@RequestBody ApiDirectory directory,HttpServletRequest request){
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            mappingFactory.removeDirectory(directory);
            mappingFactory.loadDirectoryList(true);
        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
        return ApiResult.success(null);
    }

    /**
     * 自动完成，类型获取
     */
    @GetMapping("/completion-items")
    public ApiResult provideCompletionTypes() throws Exception {
        String cacheKey = "completion-items-cache";
        CompletionResult result = null;
        if ((result = (CompletionResult) cache.get(cacheKey)) != null){
            return ApiResult.success(result);
        }

        result = new CompletionResult();
        Map<String,List<MethodVo>> clazzs = new LinkedHashMap<>();
        Map<String,String> variables = new HashMap<>();
        Map<String,String> syntax = new HashMap<>();
        Map<String,List<TableInfo>> dbInfos = new HashMap<>();
        result.setClazzs(clazzs);
        result.setVariables(variables);
        result.setSyntax(syntax);
        result.setDbInfos(dbInfos);

        //获取内置自定义函数变量
        Collection<IFunction> functionList = context.getBeansOfType(IFunction.class).values();
        functionList.forEach(item->{
            variables.put(item.getVarName(),item.getClass().getName());
        });

        //spring bean对象获取
        Map<String,Object> beans = context.getBeansOfType(Object.class);

        for (String key : beans.keySet()){
            buildClazz(clazzs,beans.get(key).getClass());
        }

        //本包JAVA类
        List<Class> classList = PackageUtil.loadClassByLoader(Thread.currentThread().getContextClassLoader());
        for (Class clazz : classList){
            buildClazz(clazzs,clazz);
        }

        //基础包 java.util java类
        List<String> classNames = PackageUtil.scan();
        for (String clazz : classNames){
            buildClazz(clazzs,clazz);
        }

        //常用语法提示
        syntax.put("foreach","for(item in ${1:collection}){\n\t\n}");
        syntax.put("fori","for(${1:i}=0;${1:i}<;${1:i}++){\n\t\n}");
        syntax.put("for","for(${1}){\n\t\n}");
        syntax.put("if","if(${1:condition}){\n\n}");
        syntax.put("ifelse","if(${1:condition}){\n\t\n}else{\n\t\n}");
        syntax.put("import","import ");
        syntax.put("continue","continue;");
        syntax.put("break","break;");

        //数据库信息获取
        Map<String, DataSourceDialect> dataSourceDialectMap = dataSourceManager.getDialectMap();
        dataSourceDialectMap.forEach((key,value)->{
            List<TableInfo> tableInfos = value.buildTableInfo();
            if (tableInfos != null){
                dbInfos.put(key,tableInfos);
            }
        });

        //常用工具类获取

        cache.put(cacheKey,result);
        return ApiResult.success(result);
    }

    private void buildClazz(Map<String, List<MethodVo>> clazzs, String clazz) {
        if (clazzs.get(clazz) != null || clazz.indexOf("$") !=-1){
            return;
        }
        clazzs.put(clazz,Collections.EMPTY_LIST);
    }

    private void buildClazz(Map<String, List<MethodVo>> clazzs, Class clazz) {
        if (clazzs.get(clazz.getName()) != null || clazz.getName().indexOf("$") !=-1){
            return;
        }
        clazzs.put(clazz.getName(),buildMethod(clazz));
    }

    /**
     * 自动完成，方法解析
     */
    @PostMapping("/completion-clazz")
    public ApiResult provideCompletionItems(@RequestBody ProvideCompletionReq completionReq){
        try {
            Class clazz = Class.forName(completionReq.getClazz());
            return ApiResult.success(buildMethod(clazz));
        }catch (Throwable e){}
        return ApiResult.success(Collections.emptyList());
    }

    private List<MethodVo> buildMethod(Class clazz){
        List<MethodVo> methodVos = new ArrayList<>();
        //成员变量
        for(Field field : clazz.getFields()){
            methodVos.add(MethodVo.builder()
                    .type("field")
                    .varName(field.getName())
                    .resultType(field.getType().getName())
                    .build());
        }

        //方法
        for (Method method : clazz.getMethods()){
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            String params = Stream.of(method.getParameters()).map(item->item.getType().getSimpleName()+" "+item.getName()).collect(Collectors.joining(","));
            methodVos.add(MethodVo.builder()
                    .type(isStatic?"static":"public")
                    .varName(method.getName())
                    .params(params)
                    .resultType(method.getReturnType().getName())
                    .build());
        }
        return methodVos;
    }
}
