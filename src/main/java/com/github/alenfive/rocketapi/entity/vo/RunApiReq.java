package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

import java.util.Map;

/**
 * @Description:脚本运行
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/25 14:31
 * @UpdateDate: 2020/6/25 14:31
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Data
public class RunApiReq {
    private boolean debug;
    private String pattern;
    private String url;
    private String options;
    private Map<String,String> header;
    private Map<String,Object> body;
    private String datasource;
    private String script;
}
