package com.github.alenfive.dataway2.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/5/22 12:45
 * @UpdateDate: 2020/5/22 12:45
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 路径实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfo {

    private String id;
    /**
     * 路径
     */
    private String path;
    /**
     * 方法支持列表
     */
    private String method;
    /**
     * 模拟参数
     */
    private String params;
    /**
     * API模式，CODE/SQL，分别为代码模式，或SQL模式
     */
    private String type;
    /**
     * 注释说明
     */
    private String comment;
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
    private String group;

    /**
     * 最后一次编辑者
     */
    private String editor;

    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    public String getScript() {
        if (this.script != null){
            try {
                return URLDecoder.decode(URLDecoder.decode(this.script,"utf-8"),"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setScript(String script) {
        if (script != null){
            try {
                this.script = URLEncoder.encode(script,"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public ApiType getType() {
        return ApiType.valueOf(this.type);
    }

    public ApiParams getParams(ObjectMapper objectMapper){
        try {
            return objectMapper.readValue(this.params,ApiParams.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String,Object> toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put("id",id);
        result.put("method",method);
        result.put("path",path);
        result.put("params",params);
        result.put("type",type);
        result.put("comment",comment);
        result.put("datasource",datasource);
        result.put("script",script);
        result.put("service",service);
        result.put("group",group);
        result.put("editor",editor);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (createTime != null){
            result.put("createTime",sdf.format(createTime));
        }
        if (updateTime != null){
            result.put("updateTime",sdf.format(updateTime));
        }
        return result;
    }
}
