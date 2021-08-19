package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import com.github.alenfive.rocketapi.entity.ApiParams;

import java.util.List;

/**
 * 分页构造器接口
 */
public interface IApiPager {

    /**
     *
     * @param totalRecords 查询得到的总记录数
     * @param data 分页列表数据
     * @param apiInfo 该API信息
     * @param apiParams 参数信息
     */
    Object buildPager(Long totalRecords, List data, ApiInfo apiInfo, ApiParams apiParams);

    /**
     * 每页大小参数名
     */
    String getPageSizeVarName();

    /**
     * 第几页参数名
     */
    String getPageNoVarName();

    /**
     * skip变量名，由页码和每页大小计算获得,
     */
    String getOffsetVarName();

    /**
     * 计算skip变量的值
     */
    Integer getOffset(Integer pageSize,Integer pageNo);

    /**
     * 获取页码
     * @return
     */
    Integer getPageNo();

    /**
     * 获取每页大小
     */
    Integer getPageSize();
}
