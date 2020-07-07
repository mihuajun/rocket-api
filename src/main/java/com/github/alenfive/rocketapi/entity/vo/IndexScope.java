package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

/**
 * 位置范围
 */
@Data
public class IndexScope {
    private String token;
    private Integer beginIndex;
    private Integer endIndex;
}
