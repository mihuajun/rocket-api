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
public class CompletionType {
    private String type;
    private String varName;

    private String label;
    private String insertText;
    List<CompletionType> funcList;
}
