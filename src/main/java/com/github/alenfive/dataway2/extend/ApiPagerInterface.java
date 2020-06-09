package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.entity.ApiInfo;
import com.github.alenfive.dataway2.entity.ApiParams;

import java.util.List;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/29 12:02
 * @UpdateDate: 2020/5/29 12:02
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 分页返回
 */
public interface ApiPagerInterface {

    Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams);

    String getPageSizeVarName();

    String getPageNoVarName();

    String getIndexVarName();

    Integer getIndexVarValue(Integer pageSize,Integer pageNo);

    Integer getPageSizeDefaultValue();

    Integer getPageNoDefaultValue();
}
