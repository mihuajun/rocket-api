package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.entity.DBConfig;

/**
 *
 */

public abstract class IDataSourceDialectDriver {
    abstract String getName();
    abstract String getIcon();
    abstract String getFormat();
    public String getDriver(){
        return this.getClass().getName();
    }
    public abstract DataSourceDialect factory(DBConfig config) throws Exception;
}
