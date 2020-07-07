package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @Description:脚本执行返回
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/26 17:01
 * @UpdateDate: 2020/6/26 17:01
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Data
public class RunApiRes {
    private List<String> logs = null;
    private Object data = null;
}
