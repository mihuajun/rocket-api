package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiDirectory;
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
public class AcceptApiInfoSyncReq {
    /**
     * 0：全量，1：增量
     */
    private Integer increment;
    private String sign;
    private Long timestamp;
    private List<ApiInfo> apiInfos;
    private List<ApiDirectory> directories;
}
