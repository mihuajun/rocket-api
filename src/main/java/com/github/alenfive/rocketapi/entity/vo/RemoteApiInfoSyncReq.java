package com.github.alenfive.rocketapi.entity.vo;

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
public class RemoteApiInfoSyncReq {
    private String remoteUrl;
    private Integer increment;
    private String secretKey;
    private List<String> apiInfoIds;
}
