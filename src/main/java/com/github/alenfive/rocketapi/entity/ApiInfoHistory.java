package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API history 实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiTable("api_info_history")
public class ApiInfoHistory extends ApiEntity{

    /**
     * api info id
     */
    private String apiInfoId;
    /**
     * 路径
     */
    private String path;
    /**
     * 完整路径
     */
    private String fullPath;
    /**
     * 所在目录
     */
    private String directoryId;
    /**
     * 方法支持列表
     */
    private String method;
    /**
     * API选项
     */
    private String options;
    /**
     * API模式，CODE/SQL，分别为代码模式，或SQL模式
     */
    private String type;
    /**
     * 注释说明
     */
    private String name;
    /**
     * 数据源
     */
    private String datasource;
    /**
     * SQL模式下的执行脚本
     */
    private String script;
    /**
     * 服务
     */
    private String service;

    /**
     * 最后一次编辑者
     */
    private String editor;

    /**
     * 创建时间
     */
    private String createTime;

}
