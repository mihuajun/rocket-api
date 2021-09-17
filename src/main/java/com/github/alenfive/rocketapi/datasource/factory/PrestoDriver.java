package com.github.alenfive.rocketapi.datasource.factory;

import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.PrestoDataSource;
import com.github.alenfive.rocketapi.entity.DBConfig;
import org.springframework.stereotype.Component;

/**
 * Presto  构造器
 */
@Component
public class PrestoDriver extends JdbcDriver {

    @Override
    public String getName() {
        return "Presto";
    }

    @Override
    public String getIcon() {
        return "rocketapi/images/Presto.png";
    }

    @Override
    public String getFormat() {
        return "jdbc:presto://127.0.0.1:8443/hive";
    }

    @Override
    public DataSourceDialect factory(DBConfig config) {
        return new PrestoDataSource(super.getDataSource(config));
    }
}
