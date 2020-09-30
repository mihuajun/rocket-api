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
 * 模拟参数实体对象
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
    private Integer elapsedTime;
    private String editor;
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
        result.put("elapsedTime", elapsedTime);
        result.put("options",options);
        result.put("editor",editor);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (createTime != null){
            result.put("createTime",sdf.format(createTime));
        }
        return result;
    }
}
