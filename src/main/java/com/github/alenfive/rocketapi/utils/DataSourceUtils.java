package com.github.alenfive.rocketapi.utils;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;

public class DataSourceUtils {
    /**
     * 关闭数据源
     * @param dataSource
     */
    public static void closeDataSource(DataSource dataSource){
        if (dataSource == null){
            return;
        }
        if (!(dataSource instanceof Closeable)){
            return;
        }
        try {
            ((Closeable)dataSource).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
