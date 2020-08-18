package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionResult {

    /**
     * java类描述
     */
    private Map<String,List<MethodVo>> clazzs;

    /**
     * 变量描述
     */
    private Map<String,String> variables;

    /**
     * 常用语法
     */
    private Map<String,String> syntax;

    /**
     * 数据库描述
     */
    private Map<String,List<TableInfo>> dbInfos;
}
