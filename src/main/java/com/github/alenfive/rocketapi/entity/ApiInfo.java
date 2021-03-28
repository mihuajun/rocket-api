package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiTable;
import com.github.alenfive.rocketapi.annotation.ApiUpdateField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API 实体对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiTable("api_info")
public class ApiInfo extends ApiEntity{

    /**
     * 路径
     */
    @ApiUpdateField
    private String path;
    /**
     * 完整路径=directory_path+this_path
     */
    @ApiUpdateField
    private String fullPath;
    /**
     * 方法支持列表
     */
    @ApiUpdateField
    private String method;
    /**
     * API选项
     */
    @ApiUpdateField
    private String options;
    /**
     * API模式，CODE/QL，分别为代码模式，或QL模式
     */
    private String type;
    /**
     * 注释说明
     */
    @ApiUpdateField
    private String name;
    /**
     * 数据源
     */
    @ApiUpdateField
    private String datasource;
    /**
     * SQL模式下的执行脚本
     */
    @ApiUpdateField
    private String script;
    /**
     * 服务
     */
    @ApiUpdateField
    private String service;

    /**
     * 所属目录
     */
    @ApiUpdateField
    private String directoryId;

    /**
     * 最后一次编辑者
     */
    @ApiUpdateField
    private String editor;

    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 更新时间
     */
    @ApiUpdateField
    private String updateTime;

}
