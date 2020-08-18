package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 自动完成，类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClazzVo {
    /**
     * 类全名，包含包名
     */
    private String varName;
    /**
     * 方法
     */
    private List<MethodVo> methods;
}
