package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.QLRequestMappingFactory;
import com.github.alenfive.dataway2.entity.*;
import com.github.alenfive.dataway2.entity.vo.*;
import com.github.alenfive.dataway2.extend.ApiInfoContent;
import com.github.alenfive.dataway2.extend.IUserAuthorization;
import com.github.alenfive.dataway2.script.IScriptParse;
import com.github.alenfive.dataway2.utils.LoginUtils;
import com.github.alenfive.dataway2.utils.RequestUtils;
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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/22 11:03
 * @UpdateDate: 2020/5/22 11:03
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 数据接口
 */
@Slf4j
@RestController
@RequestMapping("${spring.dataway2.base-path}")
public class ApiController {

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Autowired
    private IScriptParse scriptParse;

    @Autowired
    private IUserAuthorization userAuthorization;

    /**
     * LOAD API LIST
     * @return
     */
    @GetMapping("/api-list")
    public ApiResult getPathList(){
        return  ApiResult.success(mappingFactory.getPathList());
    }

    /**
     * 单个获取
     * @return
     */
    @GetMapping("/api-info/{id}")
    public ApiResult getPathList(@PathVariable String id) throws UnsupportedEncodingException {
        ApiInfo apiInfo = mappingFactory.getPathList().stream().filter(item->item.getId().equals(id)).findFirst().orElse(null);

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

        String user = LoginUtils.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        apiInfo.setEditor(user);
        try {
            if (!StringUtils.isEmpty(apiInfo.getScript())){
                apiInfo.setScript(URLEncoder.encode(apiInfo.getScript(),"utf-8"));
            }

            mappingFactory.saveOrUpdateApiInfo(apiInfo);

            //返回主键ID
            Object id = mappingFactory.getPathList().stream().filter(item->item.getMethod().equals(apiInfo.getMethod()) && item.getPath().equals(apiInfo.getPath())).findFirst().orElse(null).getId();
            return ApiResult.success(id);
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
        String user = LoginUtils.getUser(request);
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
        String user = LoginUtils.getUser(request);
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

        String user = LoginUtils.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }

        RunApiRes runApiRes = new RunApiRes();
        try {
            ApiInfo apiInfo = ApiInfo.builder()
                    .path(runApiReq.getPattern())
                    .options(runApiReq.getOptions())
                    .datasource(runApiReq.getDatasource())
                    .script(runApiReq.getScript())
                    .build();
            decodeHeaderValue(runApiReq.getHeader());
            ApiParams apiParams = ApiParams.builder()
                    .header(runApiReq.getHeader())
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



    private void decodeHeaderValue(Map<String,String> header) throws UnsupportedEncodingException {
        for (String key : header.keySet()){
            header.put(key,URLDecoder.decode(header.get(key),"utf-8"));
        }
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

        String user = LoginUtils.getUser(request);
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
        String user = LoginUtils.getUser(request);
        if(StringUtils.isEmpty(user)){
            return ApiResult.fail("Permission denied");
        }
        return ApiResult.success(mappingFactory.deleteExampleList(deleteExamleReq.getApiExampleList()));
    }

    /**
     * 用户登录
     * @param loginReq
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ApiResult login(@RequestBody LoginReq loginReq,HttpServletRequest request){
        String user = userAuthorization.validate(loginReq.getUsername(),loginReq.getPassword());
        if (!StringUtils.isEmpty(user)){
            LoginUtils.setUser(request,user);
            return ApiResult.success(user);
        }
        return ApiResult.fail("Incorrect user name or password");
    }

    /**
     * 注销登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ApiResult login(HttpServletRequest request){
        LoginUtils.setUser(request,null);
        return ApiResult.success(null);
    }

}
