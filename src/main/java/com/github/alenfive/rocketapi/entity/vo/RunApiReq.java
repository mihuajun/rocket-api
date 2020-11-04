package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

import java.util.Map;

/**
 * 脚本在线运行入参
 */
@Data
public class RunApiReq {
    private boolean debug;
    private String pattern;
    private String url;
    private String options;
    private Map<String,String> header;
    private Object body;
    private String datasource;
    private String script;
}
