package com.github.alenfive.rocketapi.entity.vo;

import com.github.alenfive.rocketapi.entity.ApiInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshMapping {

    private ApiInfo oldMapping;

    private ApiInfo newMapping;
}
