package com.github.alenfive.rocketapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.entity.vo.IgnoreWrapper;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IApiInfoCache;
import com.github.alenfive.rocketapi.extend.IResultWrapper;
import com.github.alenfive.rocketapi.extend.IScriptEncrypt;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.ApiInfoService;
import com.github.alenfive.rocketapi.service.ConfigService;
import com.github.alenfive.rocketapi.service.DataSourceService;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import com.github.alenfive.rocketapi.utils.PackageUtils;
import com.github.alenfive.rocketapi.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    @Lazy
    private IScriptParse scriptParse;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    @Autowired
    private IApiInfoCache apiInfoCache;

    @Autowired
    private IResultWrapper resultWrapper;

    @Autowired
    private IScriptEncrypt scriptEncrypt;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ApiInfoService apiInfoService;

    /**
     * 需要先初始化
     */
    @Autowired
    private SpringContextUtils springContextUtils;

    @Autowired
    private DataSourceService dataSourceService;

    private List<String> bodyMethods = Arrays.asList("POST", "PUT", "PATCH");

    /**
     * 初始化db mapping
     */
    @PostConstruct
    public void buildInit() throws Exception {
        reInit(true);
    }

    public void reInit(Boolean isStart) throws Exception {
        //register setParseService
        dataSourceManager.setParseService(parseService);

        //load banner
        loadBanner();

        //重新加载配置
        configService.reloadApiConfig(isStart);

        //重新加载数据库API
        apiInfoService.reLoadApiInfo(isStart);

        //重新加载数据源
        try {
            dataSourceService.reLoadDBConfig(isStart);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadBanner() {
        System.out.println("__________               __           __       _____ __________.___ \n" +
                "\\______   \\ ____   ____ |  | __ _____/  |_    /  _  \\\\______   \\   |\n" +
                " |       _//  _ \\_/ ___\\|  |/ // __ \\   __\\  /  /_\\  \\|     ___/   |\n" +
                " |    |   (  <_> )  \\___|    <\\  ___/|  |   /    |    \\    |   |   |\n" +
                " |____|_  /\\____/ \\___  >__|_ \\\\___  >__|   \\____|__  /____|   |___|\n" +
                "        \\/            \\/     \\/    \\/               \\/              \n" +
                "\033[32;2m" + ":: Rocket API ::" + "\033[m" + "        (" + PackageUtils.getVersion() + ")   " + buildLocalLink());

    }

    private String buildLocalLink() {
        String content = serverProperties.getServlet().getContextPath() == null ? "" : serverProperties.getServlet().getContextPath();
        Integer port = serverProperties.getPort() == null ? 8080 : serverProperties.getPort();
        return "http://localhost:" + port + ("/" + content + rocketApiProperties.getBaseRegisterPath()).replace("//", "/");
    }


    /**
     * 执行脚本逻辑
     */
    @RequestMapping
    @ResponseBody
    public ResponseEntity execute(@PathVariable(required = false) Map<String, String> pathVar,
                                  @RequestParam(required = false) Map<String, Object> param,
                                  HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String path = RequestUtils.buildPattern(request);
        String method = request.getMethod();
        Map<String, Object> body = new HashMap<>();

        if (bodyMethods.contains(method)) {
            if (request.getContentType() != null && request.getContentType().indexOf("application/json") > -1) {
                try {
                    Object bodyObject = objectMapper.readValue(request.getInputStream(), Object.class);
                    if (bodyObject instanceof Map) {
                        body.putAll((Map<? extends String, ?>) bodyObject);
                    }
                    body.put(rocketApiProperties.getBodyRootKey(), bodyObject);
                } catch (MismatchedInputException exception) {
                    throw new HttpMessageNotReadableException("Required request body is missing", exception, new ServletServerHttpRequest(request));
                }
            } else if (request.getContentType() != null && request.getContentType().indexOf("multipart/form-data") > -1) {
                MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
                body.putAll(multipartHttpServletRequest.getMultiFileMap());
                body.put(rocketApiProperties.getBodyRootKey(), multipartHttpServletRequest.getMultiFileMap());
            } else if (request.getContentType() != null && request.getContentType().indexOf("application/x-www-form-urlencoded") > -1) {
                Map<String, List<Object>> parameterMap = new HashMap<>(request.getParameterMap().size());
                request.getParameterMap().forEach((key, values) -> {
                    parameterMap.put(key, Arrays.asList(values));
                });
                body.putAll(parameterMap);
                body.put(rocketApiProperties.getBodyRootKey(), parameterMap);
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
            Object data = scriptParse.runScript(script.toString(), apiInfo, apiParams);
            if (data instanceof ResponseEntity) {
                return (ResponseEntity) data;
            }

            if (data instanceof IgnoreWrapper) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .body(((IgnoreWrapper) data).getData());
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(resultWrapper.wrapper(data, request, response));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(resultWrapper.throwable(e, request, response));
        } finally {
            apiInfoContent.removeAll();
        }
    }



}
