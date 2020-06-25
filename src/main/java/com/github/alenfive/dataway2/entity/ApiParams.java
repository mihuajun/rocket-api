package com.github.alenfive.dataway2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/5/22 16:26
 * @UpdateDate: 2020/5/22 16:26
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiParams {
    private Map<String,String> pathVar;
    private Map<String,String> header;
    private Map<String,Object> cookie;
    private Map<String,Object> param;
    private Map<String,Object> body;
    private HttpServletRequest request;

    public ApiParams putParam(String key,Object value){
        if (param == null){
            param = new HashMap<>();
        }
        param.put(key,value);
        return this;
    }
}
