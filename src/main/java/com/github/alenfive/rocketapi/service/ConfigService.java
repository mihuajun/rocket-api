package com.github.alenfive.rocketapi.service;

import com.github.alenfive.rocketapi.config.RefreshApiConfig;
import com.github.alenfive.rocketapi.config.RocketApiProperties;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.entity.ApiConfig;
import com.github.alenfive.rocketapi.entity.ConfigType;
import com.github.alenfive.rocketapi.entity.vo.NotifyEntity;
import com.github.alenfive.rocketapi.entity.vo.NotifyEventType;
import com.github.alenfive.rocketapi.extend.IClusterNotify;
import com.github.alenfive.rocketapi.utils.GenerateId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

@Service
public class ConfigService {

    @Autowired(required = false)
    private RefreshApiConfig refreshApiConfig;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private RocketApiProperties rocketApiProperties;

    @Autowired
    @Lazy
    private IClusterNotify clusterNotify;

    /**
     * 加载配置
     *
     */
    public void reloadApiConfig(Boolean isStart) {

        refreshConfig();

        if (!isStart){
            clusterNotify.sendNotify(NotifyEntity.builder().eventType(NotifyEventType.RefreshConfig).build());
        }
    }

    /**
     * 配置更新
     *
     * @param configContext
     * @return
     */
    public void saveYmlConfig(String configContext) throws Exception {
        ApiConfig apiConfig = this.getYmlConfig();
        if (apiConfig == null) {
            apiConfig = ApiConfig.builder()
                    .configContext(configContext)
                    .type(ConfigType.Yml.name())
                    .service(rocketApiProperties.getServiceName())
                    .build();
            apiConfig.setId(GenerateId.get().toHexString());
            dataSourceManager.getStoreApiDataSource().saveEntity(apiConfig);
        } else {
            apiConfig.setConfigContext(configContext);
            dataSourceManager.getStoreApiDataSource().updateEntityById(apiConfig);
        }

        reloadApiConfig(false);
    }

    public ApiConfig getConfigById(String id){
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setId(id);
        return dataSourceManager.getStoreApiDataSource().findEntityById(apiConfig);
    }

    public void removeConfigById(String id){
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setId(id);
        dataSourceManager.getStoreApiDataSource().removeEntityById(apiConfig);
    }

    public ApiConfig getYmlConfig(){
        List<ApiConfig> list = dataSourceManager.getStoreApiDataSource().listByEntity(ApiConfig.builder().service(rocketApiProperties.getServiceName()).type(ConfigType.Yml.name()).build());
        return list.stream().findFirst().orElse(null);
    }

    public void refreshConfig() {
        MutablePropertySources propertySources = environment.getPropertySources();
        String apiConfigName = "applicationConfig:[db:/rocket-api.yml]";
        if (!rocketApiProperties.isConfigEnabled()) {
            propertySources.remove(apiConfigName);
            return;
        }

        ApiConfig apiConfig = this.getYmlConfig();

        YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
        if (apiConfig != null && !StringUtils.isEmpty(apiConfig.getConfigContext())) {
            factoryBean.setResources(new ByteArrayResource(apiConfig.getConfigContext().getBytes()));
        }

        Properties properties = factoryBean.getObject();

        PropertiesPropertySource constants = new PropertiesPropertySource(apiConfigName, properties);

        Pattern p = Pattern.compile("^applicationConfig.*");
        String name = null;
        boolean exists = propertySources.contains(apiConfigName);

        if (exists) {
            name = apiConfigName;
        } else {
            for (PropertySource<?> source : propertySources) {
                if (p.matcher(source.getName()).matches()) {
                    name = source.getName();
                    break;
                }
            }
        }

        if (exists) {
            propertySources.replace(name, constants);
        } else if (name != null) {
            propertySources.addBefore(name, constants);
        } else {
            propertySources.addFirst(constants);
        }

        //配置刷新
        if (refreshApiConfig != null) {
            refreshApiConfig.refresh();
        }
    }
}
