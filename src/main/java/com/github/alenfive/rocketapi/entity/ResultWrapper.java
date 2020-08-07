package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 结果包装类
 */
@Data
@AllArgsConstructor
public class ResultWrapper {
    private String code;
    private String action;
    private String msg;
    private Object data;
}
