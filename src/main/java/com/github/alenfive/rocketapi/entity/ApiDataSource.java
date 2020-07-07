package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * API数据源实体对象
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDataSource {
    /**
     * 唯一标识符
     */
    private String id;
    /**
     * 是否是API存储所在数据源,有且只能有一个为true
     */
    private boolean storeApi;
    /**
     * 数据源对象
     */
    private DataSource dataSource;
}
