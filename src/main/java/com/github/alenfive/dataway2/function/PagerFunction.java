package com.github.alenfive.dataway2.function;

import com.github.alenfive.dataway2.extend.ApiInfoContent;
import com.github.alenfive.dataway2.extend.DefaultApiPager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description: 分页封装
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/23 18:51
 * @UpdateDate: 2020/6/23 18:51
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
@Component
public class PagerFunction implements IFunction{

    @Autowired
    private DefaultApiPager pager;

    @Autowired
    private ApiInfoContent apiInfoContent;

    @Override
    public String getVarName() {
        return "Pager";
    }

    public Object build(Long total, List<Map<String,Object>> list){
        return pager.buildPager(total,list,apiInfoContent.getApiInfo(),apiInfoContent.getApiParams());
    }
}
