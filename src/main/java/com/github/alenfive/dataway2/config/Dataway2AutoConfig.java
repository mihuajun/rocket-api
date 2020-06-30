package com.github.alenfive.dataway2.config;

import com.github.alenfive.dataway2.controller.ApiController;
import com.github.alenfive.dataway2.controller.ViewController;
import com.github.alenfive.dataway2.datasource.DataSourceManager;
import com.github.alenfive.dataway2.extend.*;
import com.github.alenfive.dataway2.extend.DefaultAssertException;
import com.github.alenfive.dataway2.script.GroovyScriptParse;
import com.github.alenfive.dataway2.script.IScriptParse;
import com.github.alenfive.dataway2.service.ScriptParseService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties({Dataway2Properties.class})
@ConditionalOnBean(DataSourceManager.class)
public class Dataway2AutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public ApiController getApiController(){
        return new ApiController();
    }

    @Bean
    @ConditionalOnMissingBean
    public ViewController getViewController(){
        return new ViewController();
    }

    @Bean
    @ConditionalOnMissingBean
    public QLRequestMappingFactory getApiInfoMappingFactory(){
        return new QLRequestMappingFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultApiPager getApiPager(){
        return new DefaultApiPager();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScriptParseService getScriptParseService(){
        return new ScriptParseService();
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultAssertException getDefaultAssertException(){
        return new DefaultAssertException();
    }

    @Bean
    @ConditionalOnMissingBean
    public IScriptParse getIScriptParse(){
        return new GroovyScriptParse();
    }
}
