package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.SQLRequestMappingFactory;
import com.github.alenfive.dataway2.entity.ApiExample;
import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiResult;
import com.github.alenfive.dataway2.entity.vo.RenameGroupReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
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
@RequestMapping("/dataway2")
public class ApiController {

    @Autowired
    private SQLRequestMappingFactory sqlRequestMapping;


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
    public ApiResult saveExample(@RequestBody ApiExample apiExample){

        if (StringUtils.isEmpty(apiExample.getMethod())
                || StringUtils.isEmpty(apiExample.getUrl())
                || StringUtils.isEmpty(apiExample.getRequestHeader())){
            return ApiResult.fail("Send, then Save");
        }

        apiExample.setCreateTime(new Date());
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
        return ApiResult.success(sqlRequestMapping.lastApiExample(apiInfoId,limit));
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
