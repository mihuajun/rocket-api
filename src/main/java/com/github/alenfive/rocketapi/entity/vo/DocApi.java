package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiExample;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocApi {
    private ApiInfo apiInfo;
    private ApiExample apiExample;
}
