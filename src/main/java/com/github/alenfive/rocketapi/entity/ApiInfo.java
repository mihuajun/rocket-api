package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiId;
import com.github.alenfive.rocketapi.annotation.ApiTable;
import com.github.alenfive.rocketapi.annotation.ApiUpdateField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * API 实体对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiTable("api_info")
public class ApiInfo {

    @ApiId
    private String id;
    /**
     * 路径
     */
    @ApiUpdateField
    private String path;
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
     * 分组
     */
    @ApiUpdateField
    private String groupName;

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

    public Map<String,Object> toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put("id",id);
        result.put("method",method);
        result.put("path",path);
        result.put("options", options);
        result.put("type",type);
        result.put("name", name);
        result.put("datasource",datasource);
        result.put("script",script);
        result.put("service",service);
        result.put("groupName",groupName);
        result.put("editor",editor);
        result.put("createTime",createTime);
        result.put("updateTime",updateTime);
        return result;
    }

}
