package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 方法
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodVo {
    /**
     * 类型
     * static,public,field
     */
    private String type;
    /**
     * 变量名，用于替换和匹配输入项
     */
    private String varName;
    /**
     * 返回类型，用于匹配子对象方法
     */
    private String resultType;
    /**
     * 参数，用于提示项
     */
    private String params;
}
