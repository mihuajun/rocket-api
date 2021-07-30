package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MappingVo {
    private String method;
    private String fullPath;

    private ApiInfo apiInfo;
}
