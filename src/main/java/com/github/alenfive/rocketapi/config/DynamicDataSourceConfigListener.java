package com.github.alenfive.rocketapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.datasource.DataSourceDialect;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.datasource.factory.IDataSourceDialectFactory;
import com.github.alenfive.rocketapi.entity.DataSourceProperty;
import com.github.alenfive.rocketapi.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 动态数据源监听
 */
@ConfigurationProperties(prefix = "spring.rocket-api",ignoreInvalidFields = true)
@Component
@ConditionalOnBean(DataSourceManager.class)
public class DynamicDataSourceConfigListener{

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ObjectMapper objectMapper;

    private String lastMd5 = null;

    private List<DataSourceProperty> multiDatasource;

    public void setMultiDatasource(List<DataSourceProperty> multiDatasource) {
        try {
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
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void execute() throws Exception {

        Map<String, DataSourceDialect> dialectMap = dataSourceManager.getDialectMap();
        Iterator<Map.Entry<String, DataSourceDialect>> it = dialectMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, DataSourceDialect> item = it.next();
            if (item.getValue().isDynamic()){
                it.remove();
            }
        }

        if (CollectionUtils.isEmpty(multiDatasource)){
            return;
        }

        for (DataSourceProperty item : multiDatasource){
            IDataSourceDialectFactory factory = (IDataSourceDialectFactory)(Class.forName(item.getFactoryClassName()).newInstance());
            DataSourceDialect dialect = factory.factory(buildProperties(item.getConfig()));
            dialect.setDynamic(true);

            if(dialectMap.get(item.getName()) != null){
                throw new IllegalArgumentException("datasource '"+item.getName()+"' conflict");
            }
            dialectMap.put(item.getName(),dialect);
        }
    }

    private Properties buildProperties(Map<String,Object> config){
        Properties result = new Properties();
        if (CollectionUtils.isEmpty(config)) {
            return result;
        }
        result.putAll(config);
        return result;
    }
}
