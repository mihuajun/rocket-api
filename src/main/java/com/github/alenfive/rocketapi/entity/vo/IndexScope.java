package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

/**
 * @Description: 位置范围
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/28 9:04
 * @UpdateDate: 2020/6/28 9:04
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Data
public class IndexScope {
    private String token;
    private Integer beginIndex;
    private Integer endIndex;
}
