package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;

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
public interface IApiPager {

    /**
     *
     * @param totalRecords 查询得到的总记录数
     * @param data 分页列表数据
     * @param apiInfo 该API信息
     * @param apiParams 参数信息
     * @return
     */
    Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams);

    /**
     * 每页大小参数名
     * @return
     */
    String getPageSizeVarName();

    /**
     * 第几页参数名
     * @return
     */
    String getPageNoVarName();

    /**
     * skip变量名，由页码和每页大小计算获得,
     * @return
     */
    String getIndexVarName();

    /**
     * 计算skip变量的值
     * @param pageSize
     * @param pageNo
     * @return
     */
    Integer getIndexVarValue(Integer pageSize,Integer pageNo);

    /**
     * 默认每页大小
     * @return
     */
    Integer getPageSizeDefaultValue();

    /**
     * 默认页码
     * @return
     */
    Integer getPageNoDefaultValue();
}
