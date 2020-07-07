package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Description: 数组变量对象
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/22 17:33
 * @UpdateDate: 2020/6/22 17:33
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
public class ArrVar {
    private String varName;
    private Integer index;
}
