package com.github.alenfive.rocketapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.entity.vo.Page;
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

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired(required = false)
    private RefreshApiConfig refreshApiConfig;

    private IApiPager apiPager = new SysApiPager();

    private List<String> bodyMethods = Arrays.asList("POST","PUT","PATCH");

    /**
     * 初始化db mapping
     */
    @PostConstruct
    public void init() throws Exception {
        //register setParseService
        dataSourceManager.setParseService(parseService);

        //load banner
        loadBanner();

        //加载配置
        reloadApiConfig();

        //加载数据库API
        List<ApiInfo> apiInfos = dataSourceManager.listApiInfoByEntity(ApiInfo.builder().service(service).build());
        for (ApiInfo apiInfo : apiInfos){
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            codeInfo.setCreateTime(sdf.format(new Date()));
            codeInfo.setUpdateTime(sdf.format(new Date()));
            dataSourceManager.saveApiInfo(codeInfo);
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

    private void clear(){
        apiInfoCache.getAll().stream().filter(item->ApiType.Ql.name().equals(item.getType())).forEach(item->{
            this.unregisterMappingForApiInfo(item);
        });
        apiInfoCache.removeAll();
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
        List<ApiConfig> apiConfigList = dataSourceManager.listApiConfigByEntity(ApiConfig.builder().service(service).build());
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
                    .id(GenerateId.get().toHexString())
                    .configContext(configContext)
                    .service(service)
                    .build();
            dataSourceManager.saveApiConfig(apiConfig);
        }else{
            dataSourceManager.updateApiConfig(apiConfig);
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
                          HttpServletRequest request) throws Throwable {

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
            }
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
                    .body(resultWrapper.wrapper("0","succeeded",data,request,response));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(resultWrapper.wrapper("500",e.getMessage(),null,request,response));
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
        if (StringUtils.isEmpty(apiInfo.getPath()) || apiInfo.getPath().startsWith("TEMP-")){
            return;
        }
        String pattern = apiInfo.getPath().replaceAll("/+","/");
        log.debug("Mapped [{}]{}",apiInfo.getMethod(),pattern);
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
        log.debug("Cancel Mapping [{}]{}",apiInfo.getMethod(),apiInfo.getPath());
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
        return apiInfoCache.getAll().stream().sorted(Comparator.comparing(ApiInfo::getName).thenComparing(ApiInfo::getPath)).collect(Collectors.toList());
    }

    @Transactional
    public String saveOrUpdateApiInfo(ApiInfo apiInfo) throws Exception {

        if (existsPattern(apiInfo)){
            throw new IllegalArgumentException("method: "+apiInfo.getMethod()+" path:"+apiInfo.getPath()+" already exist");
        }

        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        apiInfo.setUpdateTime(sdf.format(new Date()));
        if (dbInfo == null){
            apiInfo.setType(ApiType.Ql.name());
            apiInfo.setCreateTime(sdf.format(new Date()));
            apiInfo.setService(service);
            apiInfo.setId(GenerateId.get().toHexString());
            dataSourceManager.saveApiInfo(apiInfo);
        }else{
            apiInfo.setType(dbInfo.getType());
            apiInfo.setCreateTime(dbInfo.getCreateTime());
            apiInfo.setService(dbInfo.getService());

            dataSourceManager.updateApiInfo(apiInfo);

            //取消mapping注册
            unregisterMappingForApiInfo(dbInfo);

            //清理缓存
            apiInfoCache.remove(dbInfo);
        }

        dbInfo = dataSourceManager.findApiInfoById(apiInfo);

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
        dataSourceManager.saveApiInfoHistory(history);
    }

    private boolean existsPattern(ApiInfo apiInfo) {
        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getPath().equals(apiInfo.getPath()) && (item.getMethod().equals("All") || item.getMethod().equals(apiInfo.getMethod()))).findFirst().orElse(null);
        if (dbInfo == null || (apiInfo.getId() != null && apiInfo.getId().equals(dbInfo.getId()))){
            return false;
        }
        return true;
    }

    @Transactional
    public void deleteApiInfo(ApiInfo apiInfo) {

        ApiInfo dbInfo = apiInfoCache.getAll().stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
        if (dbInfo == null){
            return;
        }

        //清数据库
        dataSourceManager.deleteApiInfo(apiInfo);

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
                            .method("All")
                            .type(ApiType.Code.name())
                            .service(service)
                            .groupName(groupName)
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
                                .method(method.name())
                                .type(ApiType.Code.name())
                                .service(service)
                                .groupName(groupName)
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

    public Set<String> getGroupNameList() {
        return apiInfoCache.getAll().stream().map(ApiInfo::getGroupName).collect(Collectors.toSet());
    }

    public Set<String> getApiNameList(String group) {
        return apiInfoCache.getAll().stream().filter(item->group.equals(item.getGroupName()))
                .map(item->StringUtils.isEmpty(item.getName())?item.getPath():item.getName()).collect(Collectors.toSet());
    }

    @Transactional
    public Long renameGroup(RenameGroupReq renameGroupReq) throws Exception {
        List<ApiInfo> apiInfos = apiInfoCache.getAll().stream().filter(item->renameGroupReq.getOldGroupName().equals(item.getGroupName())).collect(Collectors.toList());
        for (ApiInfo apiInfo : apiInfos){
            apiInfo.setGroupName(renameGroupReq.getNewGroupName());
            dataSourceManager.updateApiInfo(apiInfo);

            //更新缓存
            apiInfoCache.put(apiInfo);
        }
        return Long.valueOf(apiInfos.size());
    }

    @Transactional
    public Object saveExample(ApiExample apiExample) {
        dataSourceManager.saveApiExample(apiExample);
        return apiExample.getId();
    }

    public List<ApiExample> listApiExampleScript(String apiInfoId, Integer pageSize,Integer pageNo) {
        Page page = Page.builder().pageNo(pageNo).pageSize(pageSize).build();
        return dataSourceManager.listApiExampleByEntity(ApiExample.builder().apiInfoId(apiInfoId).build(),apiPager, page);
    }

    @Transactional
    public void deleteExampleList(ArrayList<ApiExample> apiExampleList) {
        apiExampleList.stream().forEach(item->{
            dataSourceManager.deleteExample(item);
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
        return dataSourceManager.listApiInfoHistoryByEntity(ApiInfoHistory.builder().apiInfoId(apiInfoId).service(service).build(),apiPager,page);
    }

    @Transactional(rollbackFor = Exception.class)
    public Object apiInfoSync(List<ApiInfo> apiInfos,Boolean increment) throws Exception {

        if (CollectionUtils.isEmpty(apiInfos)){
            return 0;
        }

        Collection<ApiInfo> currApiInfos = this.getPathList(false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //全量同步
        if(!increment){

            //删除历史信息
            for (ApiInfo dbInfo : currApiInfos){
                dataSourceManager.deleteApiInfo(dbInfo);
            }
            //添加新信息
            for (ApiInfo apiInfo : apiInfos){
                apiInfo.setCreateTime(sdf.format(new Date()));
                apiInfo.setUpdateTime(sdf.format(new Date()));
                dataSourceManager.saveApiInfo(apiInfo);

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }else {
            //增量同步
            for (ApiInfo apiInfo : apiInfos){
                ApiInfo dbInfo =  currApiInfos.stream().filter(item->item.getId().equals(apiInfo.getId())).findFirst().orElse(null);
                if (dbInfo == null){
                    if (existsPattern(apiInfo)){
                        throw new IllegalArgumentException("method: "+apiInfo.getMethod()+" path:"+apiInfo.getPath()+" already exist");
                    }
                    apiInfo.setCreateTime(sdf.format(new Date()));
                    apiInfo.setUpdateTime(sdf.format(new Date()));
                    dataSourceManager.saveApiInfo(apiInfo);
                }else{
                    apiInfo.setUpdateTime(sdf.format(new Date()));
                    dataSourceManager.updateApiInfo(apiInfo);
                }

                //保存历史版本
                saveApiHistory(apiInfo);
            }
        }

        //刷新缓存
        this.getPathList(true);
        return apiInfos.size();
    }


}
