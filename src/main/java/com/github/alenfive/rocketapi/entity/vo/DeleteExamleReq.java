package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiExample;
import lombok.Data;

import java.util.ArrayList;

/**
 * 删除模拟请求数据
 */
@Data
public class DeleteExamleReq {
    ArrayList<ApiExample> apiExampleList;
}
