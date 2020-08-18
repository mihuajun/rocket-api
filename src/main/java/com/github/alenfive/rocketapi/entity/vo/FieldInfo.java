package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段描述
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldInfo {
    /**
     * 字段名
     */
    private String name;
    /**
     * 注释
     */
    private String comment;
    /**
     * 类型
     */
    private String type;
}
