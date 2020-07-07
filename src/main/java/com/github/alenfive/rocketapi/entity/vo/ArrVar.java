package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 数组变量对象
 */
@Data
@AllArgsConstructor
public class ArrVar {
    private String varName;
    private Integer index;
}
