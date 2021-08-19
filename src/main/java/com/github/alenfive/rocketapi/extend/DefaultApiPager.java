package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;
import com.github.alenfive.rocketapi.service.ScriptParseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    @Autowired
    private RocketApiProperties rocketApiProperties;

    @Override
    public Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams) {
        Map<String,Object> pager = new HashMap<>();

        Integer pageNo = this.getPageNo();
        Integer pageSize = this.getPageSize();
        Integer offset = this.getOffset(pageSize,pageNo);

        pager.put(rocketApiProperties.getPager().getTotalRecordsVarName(),totalRecords);
        pager.put(rocketApiProperties.getPager().getTotalPagesVarName(),(pageSize == null || pageNo == null || offset == null)?0:((totalRecords + pageSize - 1) / pageSize));
        pager.put(rocketApiProperties.getPager().getDataVarName(),data);
        pager.put(this.getPageNoVarName(),pageNo);
        pager.put(this.getPageSizeVarName(),pageSize);
        pager.put(this.getOffsetVarName(),offset);
        return pager;
    }

    @Override
    public String getPageSizeVarName() {
        return rocketApiProperties.getPager().getPageSizeVarName();
    }

    @Override
    public String getPageNoVarName() {
        return rocketApiProperties.getPager().getPageNoVarName();
    }

    @Override
    public String getOffsetVarName() {
        return rocketApiProperties.getPager().getOffsetVarName();
    }

    @Override
    public Integer getOffset(Integer pageSize, Integer pageNo) {
        return (pageNo-1)*pageSize;
    }

    @Override
    public Integer getPageNo() {
        Object value = parseService.buildContentScopeParamItem(null,this.getPageNoVarName());
        if (StringUtils.isEmpty(value)){
            return rocketApiProperties.getPager().getDefaultPageNoValue();
        }
        return Integer.valueOf(value.toString());
    }

    @Override
    public Integer getPageSize() {
        Object value = parseService.buildContentScopeParamItem(null,this.getPageSizeVarName());
        if (StringUtils.isEmpty(value)){
            return rocketApiProperties.getPager().getDefaultPageSizeValue();
        }
        return Integer.valueOf(value.toString());
    }
}
