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
    private String name;
    private String comment;
    private String type;
}
