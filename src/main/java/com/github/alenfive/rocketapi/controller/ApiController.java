package com.github.alenfive.rocketapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.*;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiDocSync;
import com.github.alenfive.rocketapi.extend.IScriptEncrypt;
import com.github.alenfive.rocketapi.extend.IUserAuthorization;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.*;
import com.github.alenfive.rocketapi.utils.GenerateId;
import com.github.alenfive.rocketapi.utils.RequestUtils;
import com.github.alenfive.rocketapi.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


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
    private ApiInfoContent apiInfoContent;

    @Autowired
    @Lazy
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
    private RocketApiProperties properties;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ApiInfoService apiInfoService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private CompletionService completionService;

    /**
     * LOAD API LIST
     */
    @GetMapping("/api-list")
    public ApiResult getPathList(boolean isDb) throws Exception {
        List<ApiInfo> result = apiInfoService.getPathList(isDb).stream()
                .sorted(Comparator.comparing(ApiInfo::getName).thenComparing(ApiInfo::getFullPath))
                .collect(Collectors.toList());


        result = result.stream()
                .map(item->{
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
        ApiInfo apiInfo = apiInfoService.getPathList(false).stream().filter(item->item.getId().equals(id)).findFirst().orElse(null);

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
        List<ApiInfoHistory> historyList = apiInfoService.lastApiInfo(apiInfoId,pageSize,pageNo);
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

        if(StringUtils.isEmpty(apiInfo.getDirectoryId())){
            return ApiResult.fail("A directory is a must");
        }

        apiInfo.setEditor(user);
        try {
            if (!StringUtils.isEmpty(apiInfo.getScript())){
                apiInfo.setScript(scriptEncrypt.encrypt(apiInfo.getScript()));
            }

            String apiInfoId = apiInfoService.saveApiInfo(apiInfo);

            return ApiResult.success(apiInfoId);
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
        Collection<ApiDirectory> directories = null;

        List<ApiDirectory> dbDirectories = apiInfoService.loadDirectoryList();
        if (syncReq.getIncrement() == 1){

            apiInfos = apiInfoService.getPathList(false).stream().filter(item->syncReq.getApiInfoIds().contains(item.getId())).collect(Collectors.toList());

            Set<ApiDirectory> directorySet = new HashSet<>();
            for (ApiInfo apiInfo : apiInfos){
                ApiDirectory directory = dbDirectories.stream().filter(item->item.getId().equals(apiInfo.getDirectoryId())).findFirst().orElse(null);
                apiInfoService.relationParentDirectory(directorySet,dbDirectories,directory);
            }
            directories = directorySet;
        }else{
            apiInfos = apiInfoService.getPathList(false);
            directories = dbDirectories;
        }

        try {
            //签名验证
            Map<String,Object> signMap = new HashMap<>(4);
            signMap.put("timestamp",System.currentTimeMillis());
            signMap.put("increment",syncReq.getIncrement());
            signMap.put("apiInfos",objectMapper.writeValueAsString(apiInfos));
            signMap.put("directories",objectMapper.writeValueAsString(directories));
            String sign = SignUtils.build(syncReq.getSecretKey(),signMap);
            signMap.put("apiInfos",apiInfos);
            signMap.put("directories",directories);
            signMap.put("sign",sign);

            String remoteUrl = syncReq.getRemoteUrl().endsWith("/")?syncReq.getRemoteUrl().substring(0,syncReq.getRemoteUrl().length()-1):syncReq.getRemoteUrl();
            String url = remoteUrl+(properties.getBaseRegisterPath()+"/accept-sync").replace("//","/");

            RestTemplate restTemplate = getRestTemplate();
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

    private RestTemplate getRestTemplate(){
        SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(60000);
        return new RestTemplate(factory);
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

            apiInfoService.deleteApiInfo(apiInfo);

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
        params.put(properties.getBodyRootKey(),body);
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
        Integer beginIndex = url.indexOf("/",8);
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

        return ApiResult.success(apiInfoService.saveExample(apiExample));
    }

    /**
     * 查询最近一次模拟数据
     */
    @GetMapping("/api-example/last")
    public ApiResult lastApiExample(String apiInfoId,Integer pageSize,Integer pageNo) throws Exception {

        List<ApiExample> result = apiInfoService.listApiExampleScript(apiInfoId,pageSize,pageNo);
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
        apiInfoService.deleteExampleList(deleteExamleReq.getApiExampleList());
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
        Collection<ApiInfo> apiInfos = apiInfoService.getPathList(false);
        String result = null;
        List<ApiDirectory> directoryList = apiInfoService.loadDirectoryList();
        List<DocApi> docsInfoList = null;
        if (!StringUtils.isEmpty(apiInfoId)){

            ApiInfo apiInfo = apiInfos.stream().filter(item->item.getId().equals(apiInfoId)).findFirst().orElse(null);
            ApiExample apiExample = buildLastApiExample(apiInfo.getId());
            docsInfoList = Arrays.asList(new DocApi(apiInfo,apiExample));
        }else{
            docsInfoList = apiInfos.stream().map(item->new DocApi(item,buildLastApiExample(item.getId()))).collect(Collectors.toList());
        }
        result = apiDocSync.sync(new DocsInfo(directoryList,docsInfoList));
        return ApiResult.success(result);
    }

    private ApiExample buildLastApiExample(String apiInfoId) {
        List<ApiExample> result = apiInfoService.listApiExampleScript(apiInfoId,1,1);
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
            result = configService.getYmlConfig();
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
            configService.saveYmlConfig(configContext);
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
        return ApiResult.success(apiInfoService.loadDirectoryList().stream()
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
            apiInfoService.saveDirectory(directory);
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
            apiInfoService.removeDirectory(directory);
        } catch (Exception e) {
            return ApiResult.fail(e.getMessage());
        }
        return ApiResult.success(null);
    }

    /**
     * 接口导出
     * @param exportReq
     * @return
     */
    @PostMapping("/export")
    public void exportApi(ExportReq exportReq,HttpServletRequest request,HttpServletResponse response) throws Exception {

        String user = loginService.getUser(exportReq.getToken());


        String resStr = null;
        if(StringUtils.isEmpty(user)){
            resStr = objectMapper.writeValueAsString(ApiResult.fail("Permission denied"));
            response.getOutputStream().write(resStr.getBytes());
            return;
        }

        ExportRes exportRes = apiInfoService.exportApi(exportReq);

        response.setCharacterEncoding("UTF-8");
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(exportReq.getFileName(), "UTF-8")+".json");
        response.addHeader("Content-Type","application/octet-stream");
        resStr = objectMapper.writeValueAsString(exportRes);
        response.getOutputStream().write(resStr.getBytes());
    }

    /**
     * 接口导入
     * @param file
     * @param request
     * @param override 0：增量，1：覆盖
     * @return
     */
    @PostMapping("/import")
    public ApiResult importApiInfo(MultipartFile file,Integer override,HttpServletRequest request){
        String user = loginService.getUser(request);

        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        if (file == null){
            return ApiResult.fail("file is null");
        }

        try {
            ExportRes exportRes = objectMapper.readValue(file.getBytes(),ExportRes.class);
            Object result = apiInfoService.importAPI(exportRes.getDirectories(),exportRes.getApiInfos(),override == 1);
            return ApiResult.success(result);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }

    /**
     * 自动完成，类型获取
     */
    @GetMapping("/completion-items")
    public ApiResult provideCompletionTypes() throws Exception {
        return ApiResult.success(completionService.provideCompletionTypes());
    }



    /**
     * 自动完成，方法解析
     */
    @PostMapping("/completion-clazz")
    public ApiResult provideCompletionItems(@RequestBody ProvideCompletionReq completionReq){
        try {
            Class clazz = Class.forName(completionReq.getClazz());
            return ApiResult.success(completionService.buildMethod(clazz));
        }catch (Throwable e){}
        return ApiResult.success(Collections.emptyList());
    }



    @GetMapping("/db-config/list")
    public ApiResult listDbConfig(){
        try {
            List<DBConfig> dbConfigs = dataSourceService.getDBConfig();
            return ApiResult.success(dbConfigs);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }

    /**
     * 添加数据源
     * @param config
     */
    @PostMapping("/db-config")
    public ApiResult saveDBConfig(@RequestBody DBConfig config,HttpServletRequest request) {

        String user = loginService.getUser(request);

        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            return ApiResult.success(dataSourceService.saveDBConfig(config));
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }

    /**
     * 删除数据源
     * @param config
     */
    @DeleteMapping("/db-config")
    public ApiResult deleteDBConfig(@RequestBody DBConfig config,HttpServletRequest request) throws IOException {

        String user = loginService.getUser(request);

        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            dataSourceService.deleteDBConfig(config);
            return ApiResult.success(null);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }

    @GetMapping("/db-driver/list")
    public ApiResult listDbDriver(){
        try {
            return ApiResult.success(completionService.getDriver());
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }

    /**
     * 测试数据源
     * @param config
     */
    @PostMapping("/db-test")
    public ApiResult testDBConfig(@RequestBody DBConfig config,HttpServletRequest request) {

        String user = loginService.getUser(request);

        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        try {
            dataSourceService.testDBConfig(config);
            return ApiResult.success(null);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }
    }

    @GetMapping("/check-version")
    public ApiResult checkVersion(){
        try {
            String urlStr = "https://img.shields.io/maven-metadata/v.json?label=maven-central&metadataUrl=https://repo1.maven.org/maven2/com/github/alenfive/rocket-api-boot-starter/maven-metadata.xml";

            HttpHeaders headers = new HttpHeaders();
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

            HttpEntity<Resource> httpEntity = new HttpEntity<>(headers);

            SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(60000);
            factory.setReadTimeout(60000);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<Object> response = restTemplate.exchange(urlStr, HttpMethod.GET,
                    httpEntity, Object.class);
            return ApiResult.success(response.getBody());
        }catch (Exception e){
            return ApiResult.fail(e.getMessage());
        }

    }
}
