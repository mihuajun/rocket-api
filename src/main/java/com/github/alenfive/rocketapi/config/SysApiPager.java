package com.github.alenfive.rocketapi.config;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.extend.IApiPager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统分页数据构建器
 */
public class SysApiPager implements IApiPager {


    @Override
    public Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams) {
        Map<String,Object> pager = new HashMap<>();
        pager.put("totalRecords",totalRecords);
        pager.put("data",data);
        return pager;
    }

    @Override
    public String getPageSizeVarName() {
        return "pageSize";
    }

    @Override
    public String getPageNoVarName() {
        return "pageNo";
    }

    @Override
    public String getOffsetVarName() {
        return "index";
    }

    @Override
    public Integer getOffset(Integer pageSize, Integer pageNo) {
        return (pageNo-1)*pageSize;
    }

    @Override
    public Integer getPageNo() {
        return null;
    }

    @Override
    public Integer getPageSize() {
        return null;
    }
}
