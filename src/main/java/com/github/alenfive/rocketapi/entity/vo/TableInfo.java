package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表信息描述
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {
    /**
     * 表名
     */
    private String name;
    /**
     * 注释
     */
    private String comment;
    /**
     * 字段描述
     */
    List<FieldInfo> fields;
}
