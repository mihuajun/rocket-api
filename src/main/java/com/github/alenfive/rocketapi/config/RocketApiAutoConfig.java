package com.github.alenfive.rocketapi.config;

import com.github.alenfive.rocketapi.controller.ApiController;
import com.github.alenfive.rocketapi.controller.RemoteController;
import com.github.alenfive.rocketapi.controller.ViewController;
import com.github.alenfive.rocketapi.datasource.DataSourceManager;
import com.github.alenfive.rocketapi.datasource.factory.*;
import com.github.alenfive.rocketapi.extend.*;
import com.github.alenfive.rocketapi.function.*;
import com.github.alenfive.rocketapi.script.GroovyScriptParse;
import com.github.alenfive.rocketapi.script.IScriptParse;
import com.github.alenfive.rocketapi.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 自动配置类
 */
@Configuration
@EnableConfigurationProperties({RocketApiProperties.class})
@ConditionalOnBean(DataSourceManager.class)
public class RocketApiAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);
    }

    private CorsConfiguration buildConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 1允许任何域名使用
        corsConfiguration.addAllowedOrigin("*");
        // 2允许任何头
        corsConfiguration.addAllowedHeader("*");
        // 3允许任何方法（post、get等）
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    @Bean
    @ConditionalOnMissingBean
    public RemoteController getRemoteController(){
        return new RemoteController();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.rocket-api.view-enabled",havingValue = "true",matchIfMissing = true)
    public ApiController getApiController(){
        return new ApiController();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.rocket-api.view-enabled",havingValue = "true",matchIfMissing = true)
    public ViewController getViewController(){
        return new ViewController();
    }

    @Bean
    @ConditionalOnMissingBean
    public QLRequestMappingFactory getQLRequestMappingFactory(){
        return new QLRequestMappingFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public IApiPager getApiPager(){
        return new DefaultApiPager();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScriptParseService getScriptParseService(){
        return new ScriptParseService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IResultWrapper getIResultWrapper(){
        return new DefaultResultWrapper();
    }
    @Bean
    @ConditionalOnMissingBean
    public IAssertException getDefaultAssertException(){
        return new DefaultAssertException();
    }

    @Bean
    @ConditionalOnMissingBean
    public IScriptParse getIScriptParse(){
        return new GroovyScriptParse();
    }

    @Bean
    @ConditionalOnMissingBean
    public IUserAuthorization getIUserAuthorization(){
        return new DefaultUserAuthorization();
    }

    @Bean
    @ConditionalOnMissingBean
    public IScriptEncrypt getIScriptEncrypt(){
        return new DefaultScriptEncrypt();
    }

    @Bean
    @ConditionalOnMissingBean
    public IApiDocSync getIApiSync(){
        return new DefaultApiDocSync();
    }

    @Bean
    @ConditionalOnMissingBean
    public IApiInfoCache getIApiCache(){
        return new DefaultApiInfoCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptChangeService getEncryptChangeService(){
        return new EncryptChangeService();
    }

    @Bean
    @ConditionalOnMissingBean
    public LoginService getLoginService(){
        return new LoginService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiInfoContent getApiInfoContent(){
        return new ApiInfoContent();
    }

    @Bean
    @ConditionalOnMissingBean
    public AssertFunction getAssertFunction(){
        return new AssertFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public IDBCache getDBCache(){
        return new DefaultDBCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public DbFunction getDbFunction(){
        return new DbFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoFunction getMongoFunction(){
        return new MongoFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public EnvFunction getEnvFunction(){
        return new EnvFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public LogFunction getLogFunction(){
        return new LogFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public PagerFunction getPagerFunction(){
        return new PagerFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public UtilsFunction getUtilsFunction(){
        return new UtilsFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextFunction getContextFunction(){
        return new ContextFunction();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContextUtils getSpringContextUtils(ApplicationContext applicationContext){
        return new SpringContextUtils(applicationContext);
    }

    @Bean
    @ConditionalOnProperty(value = "spring.rocket-api.config-enabled",havingValue = "true")
    public RefreshApiConfig getRefreshApiConfig(){
        return new RefreshApiConfig();
    }

    @Bean
    @ConditionalOnMissingBean
    public ISQLInterceptor getSQLInterceptor(){
        return new DefaultSQLInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestMappingService getRequestMappingService(){
        return new RequestMappingService();
    }

    @Bean
    @ConditionalOnMissingBean
    public CompletionService getCompletionService(){
        return new CompletionService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConfigService getConfigService(){
        return new ConfigService();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceService getDataSourceService(){
        return new DataSourceService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiInfoService getApiInfoService(){
        return new ApiInfoService();
    }

    @Bean
    @ConditionalOnMissingBean
    public ClickHouseDriver getClickHouseDriver(){
        return new ClickHouseDriver();
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoDriver getMongoDriver(){
        return new MongoDriver();
    }

    @Bean
    @ConditionalOnMissingBean
    public MySQLDriver getMySQLDriver(){
        return new MySQLDriver();
    }

    @Bean
    @ConditionalOnMissingBean
    public OracleDriver getOracleDriver(){
        return new OracleDriver();
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgreSQLDriver getPostgreSQLDriver(){
        return new PostgreSQLDriver();
    }

    @Bean
    @ConditionalOnMissingBean
    public SQLServerDriver getSQLServerDriver(){
        return new SQLServerDriver();
    }

    @Bean
    @ConditionalOnMissingBean
    public IClusterNotify getClusterNotify(){
        return new DefaultClusterNotify();
    }
}
