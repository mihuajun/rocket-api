package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.entity.DBConfig;

import java.io.Serializable;

/**
 *
 */

public abstract class IDataSourceDialectDriver implements Serializable {
    public abstract String getName();
    public abstract String getIcon();
    public abstract String getFormat();
    public String getDriver(){
        return this.getClass().getName();
    }
    public abstract DataSourceDialect factory(DBConfig config) throws Exception;
}
