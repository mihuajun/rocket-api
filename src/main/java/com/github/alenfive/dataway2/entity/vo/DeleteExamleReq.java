package com.github.alenfive.dataway2.entity.vo;

import com.github.alenfive.dataway2.entity.ApiExample;
import lombok.Data;

import java.util.ArrayList;

/**
 * @Description: 删除模拟请求数据
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/7/3 15:05
 * @UpdateDate: 2020/7/3 15:05
 * @UpdateRemark: init
 * @Version: 1.0
 */
@Data
public class DeleteExamleReq {
    ArrayList<ApiExample> apiExampleList;
}
