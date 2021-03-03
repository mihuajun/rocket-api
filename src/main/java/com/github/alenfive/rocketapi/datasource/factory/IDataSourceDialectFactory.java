package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;

import java.util.Map;
import java.util.Properties;

/**
 *
 */

public interface IDataSourceDialectFactory {
    abstract DataSourceDialect factory(Properties config) throws Exception;
}
