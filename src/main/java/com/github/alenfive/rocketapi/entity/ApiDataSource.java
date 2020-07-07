package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 6/6/2020 4:32 PM
 * @UpdateDate: 6/6/2020 4:32 PM
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu API数据源
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
