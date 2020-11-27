package com.github.alenfive.rocketapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.datasource.factory.IDataSourceDialectFactory;
import com.github.alenfive.rocketapi.entity.DataSourceProperty;
import com.github.alenfive.rocketapi.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 动态数据源监听
 */
@ConfigurationProperties(prefix = "spring.rocket-api",ignoreInvalidFields = true)
@Component
public class DynamicDataSourceConfigListener{

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ObjectMapper objectMapper;

    private String lastMd5 = null;

    private List<DataSourceProperty> multiDatasource;

    public void setMultiDatasource(List<DataSourceProperty> multiDatasource) throws Exception {

        String currMd5 = null;
        if (!CollectionUtils.isEmpty(multiDatasource)){
            currMd5 = MD5Utils.getMD5Str(objectMapper.writeValueAsString(multiDatasource));
        }
        if (lastMd5 == null && currMd5 == null){
            return;
        }else if(lastMd5 != null && currMd5 != null && lastMd5.equals(currMd5)){
            return;
        }

        this.multiDatasource = multiDatasource;
        execute();
        lastMd5 = currMd5;
    }

    public void execute() throws Exception {

        Map<String, DataSourceDialect> dialectMap = dataSourceManager.getDialectMap();
        dialectMap.keySet().stream().forEach(key->{
            if (dialectMap.get(key).isDynamic()){
                dialectMap.remove(key);
            };
        });

        if (CollectionUtils.isEmpty(multiDatasource)){
            return;
        }

        for (DataSourceProperty item : multiDatasource){
            IDataSourceDialectFactory factory = (IDataSourceDialectFactory) SpringContextUtils.getApplicationContext().getBean(Class.forName(item.getFactoryClassName()));
            DataSourceDialect dialect = factory.factory(item.getConfig());
            dialect.setDynamic(true);

            if(dialectMap.get(item.getName()) != null){
                throw new IllegalArgumentException("datasource '"+item.getName()+"' conflict");
            }
            dialectMap.put(item.getName(),dialect);
        }
    }
}
