package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认分页数据构建器
 */
@Component
public class DefaultApiPager implements IApiPager {
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
    public String getIndexVarName() {
        return "index";
    }

    @Override
    public Integer getIndexVarValue(Integer pageSize,Integer pageNo) {
        return (pageNo-1)*pageSize;
    }

    @Override
    public Integer getPageSizeDefaultValue() {
        return 15;
    }

    @Override
    public Integer getPageNoDefaultValue() {
        return 1;
    }
}
