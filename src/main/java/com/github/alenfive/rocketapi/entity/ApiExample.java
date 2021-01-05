package com.github.alenfive.rocketapi.entity;

import com.github.alenfive.rocketapi.annotation.ApiId;
import com.github.alenfive.rocketapi.annotation.ApiTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟参数实体对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiTable("api_example")
public class ApiExample {
    @ApiId
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
    private String createTime;

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
        result.put("createTime",createTime);
        return result;
    }
}
