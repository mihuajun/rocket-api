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
    private String name;
    private String comment;
    List<FieldInfo> fields;
}
