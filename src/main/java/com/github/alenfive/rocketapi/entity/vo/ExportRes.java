package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiDirectory;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExportRes {
    private Collection<ApiDirectory> directories;
    private Collection<ApiInfo> apiInfos;
}
