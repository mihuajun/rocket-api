package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;

import java.util.Map;

/**
 *
 */

public interface IDataSourceDialectFactory {
    abstract DataSourceDialect factory(Map<String,Object> config) throws Exception;
}
