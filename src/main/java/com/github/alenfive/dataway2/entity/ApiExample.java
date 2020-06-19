package com.github.alenfive.dataway2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/6/19 16:41
 * @UpdateDate: 2020/6/19 16:41
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 模拟参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiExample {
    private String id;
    private String apiInfoId;
    private String url;
    private String method;
    private String requestHeader;
    private String requestBody;
    private String responseHeader;
    private String responseBody;
    private String status;
    private Integer time;
    private String options;
    private Date createTime;

    public Map toMap(){
        Map<String,Object> result = new HashMap<>();
        result.put("id",id);
        result.put("apiInfoId",apiInfoId);
        result.put("url",url);
        result.put("method", method);
        result.put("requestHeader",requestHeader);
        result.put("requestBody",requestBody);
        result.put("responseHeader",responseHeader);
        result.put("responseBody",responseBody);
        result.put("status",status);
        result.put("time",time);
        result.put("options",options);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (createTime != null){
            result.put("createTime",sdf.format(createTime));
        }
        return result;
    }
}
