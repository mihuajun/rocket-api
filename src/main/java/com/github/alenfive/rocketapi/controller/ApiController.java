package com.github.alenfive.rocketapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.*;
import com.github.alenfive.rocketapi.entity.vo.*;
import com.github.alenfive.rocketapi.extend.ApiInfoContent;
import com.github.alenfive.rocketapi.extend.IUserAuthorization;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.LoginService;
import com.github.alenfive.rocketapi.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Api ui 数据接口
 */
@Slf4j
@RestController
@RequestMapping("${spring.rocket-api.base-register-path:/interface-ui}")
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

    /**
     * LOAD API LIST
     * @return
     */
    @GetMapping("/api-list")
    public ApiResult getPathList(boolean isDb) throws Exception {
        return  ApiResult.success(mappingFactory.getPathList(isDb));
    }

    /**
     * 单个获取
     * @return
     */
    @GetMapping("/api-info/{id}")
    public ApiResult getPathList(@PathVariable String id) throws Exception {
        ApiInfo apiInfo = mappingFactory.getPathList(false).stream().filter(item->item.getId().equals(id)).findFirst().orElse(null);

        if (apiInfo == null || StringUtils.isEmpty(apiInfo.getScript())){
            return ApiResult.success(apiInfo);
        }

        ApiInfo resultInfo = new ApiInfo();
        BeanUtils.copyProperties(apiInfo,resultInfo);
        resultInfo.setScript(URLDecoder.decode(resultInfo.getScript(),"utf-8"));
        return ApiResult.success(resultInfo);
    }

    /**
     * 历史记录查询
     * @return
     */
    @GetMapping("/api-info/last")
    public ApiResult lastApiInfo(String apiInfoId,Integer pageSize,Integer pageNo) throws Exception {
        Integer index = (pageNo-1)*pageSize;
        List<ApiInfoHistory> historyList = mappingFactory.lastApiInfo(apiInfoId,index,pageSize);
        for (ApiInfoHistory history : historyList) {
            history.setScript(URLDecoder.decode(history.getScript(),"utf-8"));
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
                apiInfo.setScript(URLEncoder.encode(apiInfo.getScript(),"utf-8"));
            }

            return ApiResult.success(mappingFactory.saveOrUpdateApiInfo(apiInfo));
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }

    }

    /**
     * change group name
     * @param renameGroupReq
     * @return
     */
    @PutMapping("/api-info/group")
    public ApiResult renameGroup(@RequestBody RenameGroupReq renameGroupReq,HttpServletRequest request) throws Exception {
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }
        return ApiResult.success(mappingFactory.renameGroup(renameGroupReq));
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
            return ApiResult.success(mappingFactory.deleteApiInfo(apiInfo));
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }

    }

    /**
     * 脚本执行
     * @param runApiReq
     * @return
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
                    .path(runApiReq.getPattern())
                    .options(runApiReq.getOptions())
                    .datasource(runApiReq.getDatasource())
                    .script(runApiReq.getScript())
                    .build();
            ApiParams apiParams = ApiParams.builder()
                    .header(decodeHeaderValue(runApiReq.getHeader()))
                    .pathVar(getPathVar(runApiReq.getPattern(),runApiReq.getUrl()))
                    .param(getParam(runApiReq.getUrl()))
                    .body(runApiReq.getBody())
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
     * 组名获取
     * @return
     */
    @GetMapping("/group-name-list")
    public ApiResult getGroupNameList(){
        return ApiResult.success(mappingFactory.getGroupNameList());
    }

    /**
     * API名获取
     * @return
     */
    @GetMapping("/api-name-list")
    public ApiResult getApiNameList(String group){
        return ApiResult.success(mappingFactory.getApiNameList(group));
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
        apiExample.setCreateTime(new Date());

        if (!StringUtils.isEmpty(apiExample.getResponseBody())){
            apiExample.setResponseBody(URLEncoder.encode(apiExample.getResponseBody(),"utf-8"));
        }

        return ApiResult.success(mappingFactory.saveExample(apiExample));
    }

    /**
     * 查询最近一次模拟数据
     * @param apiInfoId
     * @return
     */
    @GetMapping("/api-example/last")
    public ApiResult lastApiExample(String apiInfoId,Integer limit) throws Exception {
        List<Map<String,Object>> result = mappingFactory.lastApiExample(apiInfoId,limit);
        result.forEach(item->{
            if (!StringUtils.isEmpty(item.get("responseBody"))){
                try {
                    item.put("responseBody",URLDecoder.decode(item.get("responseBody").toString(),"utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        return ApiResult.success(result);
    }

    /**
     * 删除模拟数据
     * @param deleteExamleReq
     * @return
     */
    @DeleteMapping("/api-example")
    private ApiResult deleteExampleList(@RequestBody DeleteExamleReq deleteExamleReq, HttpServletRequest request) throws Exception {
        String user = loginService.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }
        return ApiResult.success(mappingFactory.deleteExampleList(deleteExamleReq.getApiExampleList()));
    }

    /**
     * 用户登录
     * @param loginVo
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ApiResult login(@RequestBody LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String user = userAuthorization.validate(loginVo.getUsername(), loginVo.getPassword());
        if (!StringUtils.isEmpty(user)){
            return ApiResult.success(loginService.getToken(loginVo));
        }
        return ApiResult.fail("Incorrect user name or password");
    }

    /**
     * 注销登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ApiResult login(HttpServletRequest request,HttpServletResponse response){
        return ApiResult.success(null);
    }

}
