package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/7/15 17:12
 * @UpdateDate: 2020/7/15 17:12
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 结果包装类
 */
@Data
@AllArgsConstructor
public class ResultWrapper {
    private String code;
    private String action;
    private String msg;
    private Object data;
}
