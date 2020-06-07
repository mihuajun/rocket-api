package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.SQLRequestMappingFactory;
import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
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
    public ApiResult getPathList(@PathVariable Integer id){
        return  ApiResult.success(sqlRequestMapping.getPathList().stream().filter(item->item.getId() == id).findFirst().orElse(null));
    }

    /**
     * SAVE APIINFO
     * @param apiInfo
     */
    @PostMapping("/api-info")
    public ApiResult saveOrUpdateApiInfo(@RequestBody ApiInfo apiInfo) throws IOException {

        try {
            if (apiInfo.getScript() != null){
                apiInfo.setScript(apiInfo.getScript()
                        .replace("'","\\'")
                        .replace("\"","\\\"")
                        .replace("{","\\{")
                        .replace("}","\\}")
                        .replace("#","\\#")
                );
            }
            sqlRequestMapping.saveOrUpdateApiInfo(apiInfo);

            //返回主键ID
            Integer id = sqlRequestMapping.getPathList().stream().filter(item->item.getMethod().equals(apiInfo.getMethod()) && item.getPath().equals(apiInfo.getPath())).findFirst().orElse(null).getId();
            return ApiResult.success(id);
        }catch (Exception e){
            e.printStackTrace();
            return ApiResult.fail(e.getMessage());
        }

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
}
