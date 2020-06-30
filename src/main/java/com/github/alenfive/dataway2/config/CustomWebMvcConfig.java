package com.github.alenfive.dataway2.config;

import com.github.alenfive.dataway2.extend.DefaultApiInfoInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/6/30 16:53
 * @UpdateDate: 2020/6/30 16:53
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
@Configuration
public class CustomWebMvcConfig extends WebMvcConfigurationSupport {

    @Autowired
    private QLRequestMappingFactory mappingFactory;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DefaultApiInfoInterceptor(mappingFactory));
        super.addInterceptors(registry);
    }
}
