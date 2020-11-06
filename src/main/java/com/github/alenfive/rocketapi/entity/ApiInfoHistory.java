package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * API history 实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoHistory {


    private String id;

    /**
     * api info id
     */
    private String apiInfoId;
    /**
     * 路径
     */
    private String path;
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
     * 分组
     */
    private String groupName;

    /**
     * 最后一次编辑者
     */
    private String editor;

    /**
     * 创建时间
     */
    private String createTime;

    public Map<String,Object> toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put("id",id);
        result.put("apiInfoId",apiInfoId);
        result.put("method",method);
        result.put("path",path);
        result.put("options", options);
        result.put("type",type);
        result.put("name", name);
        result.put("datasource",datasource);
        result.put("script",script);
        result.put("service",service);
        result.put("groupName", groupName);
        result.put("editor",editor);
        result.put("createTime",createTime);
        return result;
    }
}
