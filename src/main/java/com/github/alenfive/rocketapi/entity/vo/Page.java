package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Page {
    private Integer pageSize;
    private Integer pageNo;
}
