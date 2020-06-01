package com.github.alenfive.dataway2.controller;

import com.github.alenfive.dataway2.config.SQLRequestMappingFactory;
import com.github.alenfive.dataway2.entity.ApiType;
import com.github.alenfive.dataway2.extend.DataSourceDialect;
import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.service.ScriptParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

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
    private ApplicationContext appContext;

    @Autowired
    private SQLRequestMappingFactory sqlRequestMapping;


    /**
     * LOAD DATASOURCE
     * @return
     */
    @GetMapping("/datasource-list")
    public String[] getDbList(){
        return appContext.getBeanNamesForType(DataSource.class);
    }

    /**
     * LOAD API LIST
     * @return
     */
    @GetMapping("/api-list")
    public Collection<ApiInfo> getPathList(){
        return  sqlRequestMapping.getPathList();
    }

    /**
     * SAVE APIINFO
     * @param apiInfo
     */
    @PostMapping("/api-info")
    public void saveOrUpdateApiInfo(@RequestBody ApiInfo apiInfo) throws IOException {
        apiInfo.setType(ApiType.Sql.name());
        sqlRequestMapping.saveOrUpdateApiInfo(apiInfo);
    }

    /**
     * REMOVE APIINFO
     * @param apiInfo
     */
    @DeleteMapping("/api-info")
    public void deleteApiInfo(@RequestBody ApiInfo apiInfo){
        sqlRequestMapping.deleteApiInfo(apiInfo);
    }

    @GetMapping("/group-list")
    public List<String> getGroupList(){
        return sqlRequestMapping.getGroupList();
    }
}
