package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * API远程同步入参
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiInfoSyncReq {
    private String sign;
    private long timestamp;
    private List<ApiInfo> apiInfos;
}
