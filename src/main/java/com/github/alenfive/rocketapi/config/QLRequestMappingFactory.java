package com.github.alenfive.rocketapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.entity.vo.Page;
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
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
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

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired(required = false)
    private RefreshApiConfig refreshApiConfig;

    private IApiPager apiPager = new SysApiPager();

    private List<String> bodyMethods = Arrays.asList("POST","PUT","PATCH");

    private List<ApiDirectory> directoryListCache;

    /**
     * 初始化db mapping
     */
    @PostConstruct
    public void buildInit() throws Exception {
        //register setParseService
        dataSourceManager.setParseService(parseService);

        //load banner
        loadBanner();

        //加载配置
        reloadApiConfig();

        //历史缓存清理
        if (apiInfoCache != null){
            apiInfoCache.getAll().stream().filter(item->ApiType.Ql.name().equals(item.getType())).forEach(item->{
                this.unregisterMappingForApiInfo(item);
            });
            apiInfoCache.removeAll();
        }

        //加载数据库API
        List<ApiInfo> apiInfos = dataSourceManager.getStoreApiDataSource().listByEntity(ApiInfo.builder().service(service).build());
        for (ApiInfo apiInfo : apiInfos){
            apiInfoCache.put(apiInfo);
        }

        //加载目录
        directoryListCache = dataSourceManager.getStoreApiDataSource().listByEntity(ApiDirectory.builder().service(service).build());

        //加载代码方式的API
        List<ApiInfo> codeApiList = this.getPathListForCode();
        for (ApiInfo codeInfo : codeApiList){
            ApiInfo dbInfo = apiInfoCache.get(codeInfo);
            if (dbInfo != null){
                continue;
            }

            ApiDirectory directory = directoryListCache.stream().filter(item->StringUtils.isEmpty(item.getParentId()) && codeInfo.getDirectoryId().equals(item.getName())).findFirst().orElse(null);

            if (directory == null){
                directory = ApiDirectory.builder().service(service).name(codeInfo.getDirectoryId()).build();
                directory.setId(GenerateId.get().toHexString());
                dataSourceManager.getStoreApiDataSource().saveEntity(directory);
                directoryListCache.add(directory);
            }
            codeInfo.setDirectoryId(directory.getId());
            codeInfo.setId(GenerateId.get().toHexString());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            codeInfo.setCreateTime(sdf.format(new Date()));
            codeInfo.setUpdateTime(sdf.format(new Date()));
            dataSourceManager.getStoreApiDataSource().saveEntity(codeInfo);
            apiInfoCache.put(codeInfo);
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
        Integer port = serverProperties.getPort() == null?8080:serverProperties.getPort();
        return "http://localhost:"+ port + ("/"+content+ properties.getBaseRegisterPath()).replace("//","/");
    }

    /**
     * 加载配置
     * @throws Exception
     */
    public void reloadApiConfig() throws Exception {
        MutablePropertySources propertySources = environment.getPropertySources();
        String apiConfigName = "applicationConfig:[db:/rocket-api.yml]";
        if (!properties.isConfigEnabled()){
            propertySources.remove(apiConfigName);
            return;
        }

        ApiConfig apiConfig = this.getApiConfig();

        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        if (apiConfig != null && !StringUtils.isEmpty(apiConfig.getConfigContext())){
            factoryBean.setResources(new ByteArrayResource(apiConfig.getConfigContext().getBytes()));
        }

        Properties properties = factoryBean.getObject();

        PropertiesPropertySource constants = new PropertiesPropertySource(apiConfigName, properties);

        Pattern p = Pattern.compile("^applicationConfig.*");
        String name = null;
        boolean exists = propertySources.contains(apiConfigName);

        if (exists){
            name = apiConfigName;
        }else{
            for (PropertySource<?> source : propertySources) {
                if (p.matcher(source.getName()).matches()) {
                    name = source.getName();
                    break;
                }
            }
        }

        if (exists){
            propertySources.replace(name,constants);
        }else if (name != null) {
            propertySources.addBefore(name, constants);
        } else {
            propertySources.addFirst(constants);
        }

        //配置刷新
        if (refreshApiConfig != null){
            refreshApiConfig.refresh();
        }
    }



    /**
     * 配置获取
     * @return
     */
    public ApiConfig getApiConfig() {
        List<ApiConfig> apiConfigList = dataSourceManager.getStoreApiDataSource().listByEntity(ApiConfig.builder().service(service).build());
        return CollectionUtils.isEmpty(apiConfigList)?null:apiConfigList.get(0);
    }

    /**
     * 配置更新
     * @param configContext
     * @return
     */
    public void saveApiConfig(String configContext) throws Exception {
        ApiConfig apiConfig = this.getApiConfig();
        if (apiConfig == null){
            apiConfig = ApiConfig.builder()
                    .configContext(configContext)
                    .service(service)
                    .build();
            apiConfig.setId(GenerateId.get().toHexString());
            dataSourceManager.getStoreApiDataSource().saveEntity(apiConfig);
        }else{
            apiConfig.setConfigContext(configContext);
            dataSourceManager.getStoreApiDataSource().updateEntityById(apiConfig);
        }

        reloadApiConfig();
    }

    private Object buildToBean(PropertySource<?> propertySource,String prefix, Class<?> clazz) {
        if (clazz.isAssignableFrom(Collection.class)){

        }
        return null;
    }

    /**
     * 执行脚本逻辑
     */
    @RequestMapping
    @ResponseBody
    public ResponseEntity execute(@PathVariable(required = false) Map<String,String> pathVar,
                          @RequestParam(required = false) Map<String,Object> param,
                          HttpServletRequest request,HttpServletResponse response) throws Throwable {

        String path = buildPattern(request);
        String method = request.getMethod();
        Map<String,Object> body = new HashMap<>();

        if (bodyMethods.contains(method)){
            if (request.getContentType() != null && request.getContentType().indexOf("application/json") > -1){
                try {
                    Object bodyObject = objectMapper.readValue(request.getInputStream(),Object.class);
                    if (bodyObject instanceof Map){
                        body.putAll((Map<? extends String, ?>) bodyObject);
                    }
                    body.put(properties.getBodyRootKey(),bodyObject);
                }catch (MismatchedInputException exception){
                    throw new HttpMessageNotReadableException("Required request body is missing",exception,new ServletServerHttpRequest(request));
                }
            }else if(request.getContentType() != null && request.getContentType().indexOf("multipart/form-data") > -1){
                MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
                body.putAll(multipartHttpServletRequest.getMultiFileMap());
                body.put(properties.getBodyRootKey(),multipartHttpServletRequest.getMultiFileMap());
            }else if(request.getContentType() != null && request.getContentType().indexOf("application/x-www-form-urlencoded") > -1){
                Map<String,List<Object>> parameterMap = new HashMap<>(request.getParameterMap().size());
                request.getParameterMap().forEach((key,values)->{
                    parameterMap.put(key,Arrays.asList(values));
                });
                body.putAll(parameterMap);
                body.put(properties.getBodyRootKey(),parameterMap);
            }
        }

        ApiParams apiParams = ApiParams.builder()
                .pathVar(pathVar)
                .header(RequestUtils.buildHeaderParams(request))
                .param(param)
                .body(body)
                .session(RequestUtils.buildSessionParams(request))
                .request(request)
                .response(response)
                .build();


        ApiInfo apiInfo = apiInfoCache.get(ApiInfo.builder().method(method).fullPath(path).build());

        StringBuilder script = new StringBuilder(scriptEncrypt.decrypt(apiInfo.getScript()));
        try {
            Object data = scriptParse.runScript(script.toString(),apiInfo,apiParams);
            if (data instanceof ResponseEntity){
                return (ResponseEntity) data;
            }

            if (data instanceof IgnoreWrapper){
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(((IgnoreWrapper)data).getData());
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(resultWrapper.wrapper(data,request,response));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(resultWrapper.throwable(e,request,response));
        }finally {
            apiInfoContent.removeAll();
        }
    }



    public String buildPattern(HttpServletRequest request) {
        return (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    }

    /**
     * 注册mapping
     * @param apiInfo
     */
    private void registerMappingForApiInfo(ApiInfo apiInfo) throws NoSuchMethodException {
        if (ApiType.Code.name().equals(apiInfo.getType())){
            return;
        }

        String pattern = apiInfo.getFullPath();

        if (StringUtils.isEmpty(pattern) || pattern.startsWith("TEMP-")){
            return;
        }
        log.debug("Mapped [{}]{}",apiInfo.getMethod(),pattern);
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(pattern);
        RequestMethodsRequestCondition methodsRequestCondition = new RequestMethodsRequestCondition(RequestMethod.valueOf(apiInfo.getMethod()));
        RequestMappingInfo mappingInfo = new RequestMappingInfo(patternsRequestCondition,methodsRequestCondition,null,null,null,null,null);
        Method targetMethod = QLRequestMappingFactory.class.getDeclaredMethod("execute", Map.class, Map.class,HttpServletRequest.class,HttpServletResponse.class);
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

        String pattern = apiInfo.getFullPath();

        if (StringUtils.isEmpty(pattern) || pattern.startsWith("TEMP-")){
            return;
        }
        log.debug("Cancel Mapping [{}]{}",apiInfo.getMethod(),pattern);
        PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(pattern);
        RequestMethodsRequestCondition methodsRequestCondition = new RequestMethodsRequestCondition(RequestMethod.valueOf(apiInfo.getMethod()));
        RequestMappingInfo mappingInfo = new RequestMappingInfo(patternsRequestCondition,methodsRequestCondition,null,null,null,null,null);
        requestMappingHandlerMapping.unregisterMapping(mappingInfo);
    }

    public Collection<ApiInfo> getPathList(boolean isDb) throws Exception {
        if (isDb){
            buildInit();
            apiInfoCache.refreshNotify();
        }
        return apiInfoCache.getAll();
    }

    @Transactional
    public String saveApiInfo(ApiInfo apiInfo) throws Exception {

        buildFullPath(apiInfo);

        this.assertExistsPattern(apiInfo);

        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        apiInfo.setUpdateTime(sdf.format(new Date()));
        if (dbInfo == null){
            apiInfo.setType(ApiType.Ql.name());
            apiInfo.setCreateTime(sdf.format(new Date()));
            apiInfo.setService(service);
            apiInfo.setId(GenerateId.get().toHexString());
            dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);
        }else{
            apiInfo.setType(dbInfo.getType());
            apiInfo.setCreateTime(dbInfo.getCreateTime());
            apiInfo.setService(dbInfo.getService());

            dataSourceManager.getStoreApiDataSource().updateEntityById(apiInfo);

            //取消mapping注册
            unregisterMappingForApiInfo(dbInfo);

            //清理缓存
            apiInfoCache.remove(dbInfo);
        }

        dbInfo = dataSourceManager.getStoreApiDataSource().findEntityById(apiInfo);

        //入缓存
        apiInfoCache.put(dbInfo);

        //注册mapping
        this.registerMappingForApiInfo(dbInfo);

        //存储历史
        saveApiHistory(dbInfo);

        return dbInfo.getId();
    }

    private void saveApiHistory(ApiInfo dbInfo) {
        ApiInfoHistory history = new ApiInfoHistory();
        BeanUtils.copyProperties(dbInfo,history);
        history.setApiInfoId(dbInfo.getId());
        history.setId(GenerateId.get().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        history.setCreateTime(sdf.format(new Date()));
        dataSourceManager.getStoreApiDataSource().saveEntity(history);
    }

    private void assertExistsPattern(ApiInfo apiInfo) {
        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getFullPath().equals(apiInfo.getFullPath()) && (item.getMethod().equals("All") || item.getMethod().equals(apiInfo.getMethod()))).findFirst().orElse(null);
        if (dbInfo == null || (apiInfo.getId() != null && apiInfo.getId().equals(dbInfo.getId()))){
            return;
        }
        throw new IllegalArgumentException("method: "+apiInfo.getMethod()+" path:"+apiInfo.getFullPath()+" already exist");
    }

    @Transactional
    public void deleteApiInfo(ApiInfo apiInfo) {

        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
        if (dbInfo == null){
            return;
        }

        //清数据库
        dataSourceManager.getStoreApiDataSource().removeEntityById(apiInfo);

        //清缓存
        apiInfoCache.remove(dbInfo);

        //取消mapping注册
        unregisterMappingForApiInfo(dbInfo);
    }

    /**
     * 获取已注册的API地址
     */
    public List<ApiInfo> getPathListForCode(){
        Map<RequestMappingInfo, HandlerMethod> map = requestMappingHandlerMapping.getHandlerMethods();
        List<ApiInfo> result = new ArrayList<>(map.size());
        for (RequestMappingInfo info : map.keySet()) {
            String groupName = map.get(info).getBeanType().getSimpleName();
            for(String path : info.getPatternsCondition().getPatterns()){

                //过滤本身的类
                if (path.indexOf(properties.getBaseRegisterPath()) == 0 || path.equals("/error")){
                    continue;
                }

                Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
                if (methods.isEmpty()){
                    result.add(ApiInfo.builder()
                            .path(path)
                            .fullPath(path)
                            .method("All")
                            .type(ApiType.Code.name())
                            .service(service)
                            .directoryId(groupName)
                            .editor("admin")
                            .name("")
                            .datasource("")
                            .script("")
                            .options("")
                            .build());
                }else{
                    for (RequestMethod method : methods){
                        result.add(ApiInfo.builder()
                                .path(path)
                                .fullPath(path)
                                .method(method.name())
                                .type(ApiType.Code.name())
                                .service(service)
                                .directoryId(groupName)
                                .editor("admin")
                                .name("")
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

    @Transactional
    public Object saveExample(ApiExample apiExample) {
        dataSourceManager.getStoreApiDataSource().saveEntity(apiExample);
        return apiExample.getId();
    }

    public List<ApiExample> listApiExampleScript(String apiInfoId, Integer pageSize,Integer pageNo) {
        Page page = Page.builder().pageNo(pageNo).pageSize(pageSize).build();
        return dataSourceManager.getStoreApiDataSource().pageByEntity(ApiExample.builder().apiInfoId(apiInfoId).build(),apiPager, page);
    }

    @Transactional
    public void deleteExampleList(ArrayList<ApiExample> apiExampleList) {
        apiExampleList.stream().forEach(item->{
            dataSourceManager.getStoreApiDataSource().removeEntityById(item);
        });
    }

    public void addInterceptor(ApiInfoInterceptor interceptor){
        if (this.interceptors == null){
            this.interceptors = new ArrayList<>();
        }
        this.interceptors.add(interceptor);
    }

    public List<ApiInfoHistory> lastApiInfo(String apiInfoId,Integer pageSize, Integer pageNo) {
        Page page = Page.builder().pageNo(pageNo).pageSize(pageSize).build();
        return dataSourceManager.getStoreApiDataSource().pageByEntity(ApiInfoHistory.builder().apiInfoId(apiInfoId).service(service).build(),apiPager,page);
    }

    @Transactional(rollbackFor = Exception.class)
    public Object apiInfoSync(List<ApiDirectory> directories,List<ApiInfo> apiInfos,Boolean increment) throws Exception {

        if (CollectionUtils.isEmpty(apiInfos)){
            return 0;
        }

        Collection<ApiInfo> currApiInfos = this.getPathList(false);
        Collection<ApiDirectory> currDirectories = this.loadDirectoryList(false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //全量同步
        if(!increment){

            //删除历史目录
            for (ApiDirectory dbDirectory : currDirectories ){
                dataSourceManager.getStoreApiDataSource().removeEntityById(dbDirectory);
            }

            //新增历史目录
            for (ApiDirectory directory : directories){
                dataSourceManager.getStoreApiDataSource().saveEntity(directory);
            }

            //删除历史信息
            for (ApiInfo dbInfo : currApiInfos){
                dataSourceManager.getStoreApiDataSource().removeEntityById(dbInfo);
            }
            //添加新信息
            for (ApiInfo apiInfo : apiInfos){
                apiInfo.setCreateTime(sdf.format(new Date()));
                apiInfo.setUpdateTime(sdf.format(new Date()));
                dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }else {

            //目录增量同步
            for (ApiDirectory directory : directories){
                ApiDirectory dbDirectory = currDirectories.stream().filter(item->item.getId().equals(directory.getId())).findFirst().orElse(null);
                if (dbDirectory == null){
                    dataSourceManager.getStoreApiDataSource().saveEntity(directory);
                }else{
                    dataSourceManager.getStoreApiDataSource().updateEntityById(directory);
                }
            }

            //增量同步
            for (ApiInfo apiInfo : apiInfos){
                ApiInfo dbInfo =  currApiInfos.stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
                if (dbInfo == null){

                    assertExistsPattern(apiInfo);

                    apiInfo.setCreateTime(sdf.format(new Date()));
                    apiInfo.setUpdateTime(sdf.format(new Date()));
                    dataSourceManager.getStoreApiDataSource().saveEntity(apiInfo);
                }else{
                    apiInfo.setUpdateTime(sdf.format(new Date()));
                    dataSourceManager.getStoreApiDataSource().updateEntityById(apiInfo);
                }

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }

        return apiInfos.size();
    }


    public List<ApiDirectory> loadDirectoryList(boolean isDb) {
        if (isDb){
            this.directoryListCache = dataSourceManager.getStoreApiDataSource().listByEntity(ApiDirectory.builder().service(service).build());
        }
        return this.directoryListCache;
    }

    /**
     * 目录删除
     * @param directory
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeDirectory(ApiDirectory directory){
        List<ApiDirectory> directoryList = this.loadDirectoryList(false);
        //查询该节点以下级所有目录
        Set<String> directoryIds = findChildrenIds(directoryList,directory.getId());
        directoryIds.add(directory.getId());

        for (String directoryId: directoryIds){

            //目录清理
            ApiDirectory dir = new ApiDirectory();
            dir.setId(directoryId);
            dataSourceManager.getStoreApiDataSource().removeEntityById(dir);

            //目录下的api列表
            List<ApiInfo> apiInfoList = apiInfoCache.getAll().stream().filter(item->directoryId.equals(item.getDirectoryId())).collect(Collectors.toList());

            for (ApiInfo apiInfo : apiInfoList ){
                dataSourceManager.getStoreApiDataSource().removeEntityById(apiInfo);

                //取消mapping注册
                unregisterMappingForApiInfo(apiInfo);

                //缓存清理
                apiInfoCache.remove(apiInfo);
            }
        }

    }

    /**
     * 目录修改
     * @param directory
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveDirectory(ApiDirectory directory) throws Exception {

        directory.setService(service);
        //新增
        if (StringUtils.isEmpty(directory.getId())){
            directory.setId(GenerateId.get().toHexString());
            dataSourceManager.getStoreApiDataSource().saveEntity(directory);
            //缓存刷新
            this.loadDirectoryList(true);
            return;
        }

        ApiDirectory dbDirectory = dataSourceManager.getStoreApiDataSource().findEntityById(directory);

        //数据库更新
        dataSourceManager.getStoreApiDataSource().updateEntityById(directory);
        //缓存刷新
        this.loadDirectoryList(true);

        //如果path未发生修改
        if(Objects.equals(dbDirectory.getPath(),directory.getPath())){
           return;
        }

        //更新该目录下的所有api path
        List<ApiDirectory> directoryList = this.loadDirectoryList(false);
        Set<String> directoryIds = findChildrenIds(directoryList,directory.getId());
        directoryIds.add(directory.getId());
        List<ApiInfo> modifyApiInfos = apiInfoCache.getAll().stream().filter(item->directoryIds.contains(item.getDirectoryId()) && ApiType.Ql.name().equals(item.getType())).collect(Collectors.toList());

        //
        modifyApiInfos.forEach(item->{

            //数据库更新
            ApiInfo newApiInfo = new ApiInfo();
            BeanUtils.copyProperties(item,newApiInfo);

            //构建完整路径
            this.buildFullPath(newApiInfo);

            //验证是否路径是否冲突
            assertExistsPattern(newApiInfo);

            dataSourceManager.getStoreApiDataSource().updateEntityById(newApiInfo);
        });

        //缓存刷新
        for (ApiInfo item : modifyApiInfos){
            //取消mapping注册
            unregisterMappingForApiInfo(item);

            //删除缓存
            apiInfoCache.remove(item);

            //刷新更新
            ApiInfo dbInfo = dataSourceManager.getStoreApiDataSource().findEntityById(item);
            apiInfoCache.put(dbInfo);

            //添加mapping注册
            registerMappingForApiInfo(dbInfo);

            //存储历史
            saveApiHistory(dbInfo);
        }
    }

    private String formatPath(StringBuilder path){
        path.insert(0,"/");
        String result = path.toString().replaceAll("/+","/");
        if (result.length()>1 && result.endsWith("/")){
            result = result.substring(0,result.length()-1);
        }
        return result;
    }

    private void buildFullPath(ApiInfo apiInfo){
        StringBuilder path = new StringBuilder(apiInfo.getPath());
        this.recursiveFullPath(this.loadDirectoryList(false),apiInfo.getDirectoryId(),path);
        apiInfo.setFullPath(this.formatPath(path));
    }
    private void recursiveFullPath(List<ApiDirectory> directoryList, String directoryId, StringBuilder path){
        ApiDirectory directory = directoryList.stream().filter(item->item.getId().equals(directoryId)).findFirst().orElse(null);

        if (!StringUtils.isEmpty(directory.getPath())){
            path.insert(0,"/");
            path.insert(0,directory.getPath());
        }

        if (StringUtils.isEmpty(directory.getParentId())){
            return;
        }
        this.recursiveFullPath(directoryList,directory.getParentId(),path);
    }

    public void relationParentDirectory(Set<ApiDirectory> directorySet,List<ApiDirectory> directoryList,ApiDirectory directory){
        directorySet.add(directory);
        for (ApiDirectory item : directoryList){
            if (item.getId().equals(directory.getParentId())){
                directorySet.add(directory);
                this.relationParentDirectory(directorySet,directoryList,item);
            }
        }
    }

    private Set<String> findChildrenIds(List<ApiDirectory> directoryList,String directoryId){
        Set<String> result = directoryList.stream().filter(item->directoryId.equals(item.getParentId())).map(ApiDirectory::getId).collect(Collectors.toSet());
        for (String item : result){
            result.addAll(this.findChildrenIds(directoryList,item));
        }
        return result;
    }
}
