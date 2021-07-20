package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认分页数据构建器
 */
@Component
public class DefaultApiPager implements IApiPager {

    @Autowired
    private ScriptParseService parseService;

    @Override
    public Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams) {
        Map<String,Object> pager = new HashMap<>();

        Object pageSize = parseService.buildContentScopeParamItem(null,this.getPageSizeVarName());
        Object pageNo = parseService.buildContentScopeParamItem(null,this.getPageNoVarName());
        Object index = parseService.buildContentScopeParamItem(null,this.getIndexVarName());

        pager.put("totalRecords",totalRecords);
        pager.put("totalPages",(pageSize == null || pageNo == null || index == null)?0:Integer.valueOf((int) ((Integer.valueOf(totalRecords.toString()) + Integer.valueOf(pageSize.toString()) - 1) / Integer.valueOf(pageSize.toString()))));
        pager.put("data",data);
        pager.put(this.getPageNoVarName(),pageNo);
        pager.put(this.getPageSizeVarName(),pageSize);
        pager.put(this.getIndexVarName(),index);
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
