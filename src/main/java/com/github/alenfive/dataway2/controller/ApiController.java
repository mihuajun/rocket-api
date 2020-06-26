package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.SQLRequestMappingFactory;
import com.github.alenfive.dataway2.entity.ApiExample;
import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;
import com.github.alenfive.dataway2.entity.ApiResult;
import com.github.alenfive.dataway2.entity.vo.RenameGroupReq;
import com.github.alenfive.dataway2.entity.vo.RunApiReq;
import com.github.alenfive.dataway2.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.python.google.common.base.Splitter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

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
@RequestMapping("/dataway2")
public class ApiController {

    @Autowired
    private SQLRequestMappingFactory sqlRequestMapping;

    @Autowired
    private ScriptParseService parseService;

    /**
     * LOAD API LIST
     * @return
     */
    @GetMapping("/api-list")
    public ApiResult getPathList(){
        return  ApiResult.success(sqlRequestMapping.getPathList());
    }

    /**
     * 单个获取
     * @return
     */
    @GetMapping("/api-info/{id}")
    public ApiResult getPathList(@PathVariable String id) throws UnsupportedEncodingException {
        ApiInfo apiInfo = sqlRequestMapping.getPathList().stream().filter(item->item.getId().equals(id)).findFirst().orElse(null);

        if (apiInfo == null || StringUtils.isEmpty(apiInfo.getScript())){
            return ApiResult.success(apiInfo);
        }

        ApiInfo resultInfo = new ApiInfo();
        BeanUtils.copyProperties(apiInfo,resultInfo);
        resultInfo.setScript(URLDecoder.decode(resultInfo.getScript(),"utf-8"));
        return ApiResult.success(resultInfo);
    }

    /**
     * SAVE APIINFO
     * @param apiInfo
     */
    @PostMapping("/api-info")
    public ApiResult saveOrUpdateApiInfo(@RequestBody ApiInfo apiInfo) {

        try {

            if (!StringUtils.isEmpty(apiInfo.getScript())){
                apiInfo.setScript(URLEncoder.encode(apiInfo.getScript(),"utf-8"));
            }

            sqlRequestMapping.saveOrUpdateApiInfo(apiInfo);

            //返回主键ID
            Object id = sqlRequestMapping.getPathList().stream().filter(item->item.getMethod().equals(apiInfo.getMethod()) && item.getPath().equals(apiInfo.getPath())).findFirst().orElse(null).getId();
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
    public ApiResult renameGroup(@RequestBody RenameGroupReq renameGroupReq){
        sqlRequestMapping.renameGroup(renameGroupReq);
        return ApiResult.success(null);
    }

    /**
     * REMOVE APIINFO
     * @param apiInfo
     */
    @DeleteMapping("/api-info")
    public ApiResult deleteApiInfo(@RequestBody ApiInfo apiInfo){
        try {
            sqlRequestMapping.deleteApiInfo(apiInfo);
            return ApiResult.success(null);
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
    public ApiResult runScript(@RequestBody RunApiReq runApiReq){
        try {
            ApiInfo apiInfo = ApiInfo.builder().datasource(runApiReq.getDatasource()).script(runApiReq.getScript()).build();
            decodeHeaderValue(runApiReq.getHeader());
            ApiParams apiParams = ApiParams.builder()
                    .header(runApiReq.getHeader())
                    .pathVar(getPathVar(runApiReq.getPattern(),runApiReq.getUrl()))
                    .param(getParam(runApiReq.getUrl()))
                    .body(runApiReq.getBody())
                    .build();

            StringBuilder scriptContent = new StringBuilder(apiInfo.getScript());
            parseService.parse(scriptContent,apiParams);
            return ApiResult.success(sqlRequestMapping.runScript(scriptContent,apiInfo,apiParams));
        }catch (Exception e){
            return ApiResult.fail(e.getMessage());
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
        Integer index = url.indexOf("?");
        if (StringUtils.isEmpty(url) || index == -1){
            return result;
        }
        String params = url.substring(index + 1);
        result.putAll(Splitter.on("&").withKeyValueSeparator("=").split(params));
        return result;
    }

    /**
     * 组名获取
     * @return
     */
    @GetMapping("/group-name-list")
    public ApiResult getGroupNameList(){
        return ApiResult.success(sqlRequestMapping.getGroupNameList());
    }

    /**
     * API名获取
     * @return
     */
    @GetMapping("/api-name-list")
    public ApiResult getApiNameList(String group){
        return ApiResult.success(sqlRequestMapping.getApiNameList(group));
    }

    /**
     * 模拟参数保存
     */
    @PostMapping("/api-example")
    public ApiResult saveExample(@RequestBody ApiExample apiExample) throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(apiExample.getMethod())
                || StringUtils.isEmpty(apiExample.getUrl())
                || StringUtils.isEmpty(apiExample.getRequestHeader())){
            return ApiResult.fail("Send, then Save");
        }

        apiExample.setCreateTime(new Date());

        if (!StringUtils.isEmpty(apiExample.getResponseBody())){
            apiExample.setResponseBody(URLEncoder.encode(apiExample.getResponseBody(),"utf-8"));
        }

        sqlRequestMapping.saveExample(apiExample);
        return ApiResult.success(null);
    }

    /**
     * 查询最近一次模拟数据
     * @param apiInfoId
     * @return
     */
    @GetMapping("/api-example/last")
    public ApiResult lastApiExample(String apiInfoId,Integer limit){
        List<Map<String,Object>> result = sqlRequestMapping.lastApiExample(apiInfoId,limit);
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
     * @param apiExampleList
     * @return
     */
    @DeleteMapping("/api-example")
    private ApiResult deleteExampleList(@RequestBody ArrayList<ApiExample> apiExampleList){
        sqlRequestMapping.deleteExampleList(apiExampleList);
        return ApiResult.success(null);
    }

}
