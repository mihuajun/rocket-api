package com.github.alenfive.dataway2.extend;

import com.github.alenfive.dataway2.config.QLRequestMappingFactory;
import com.github.alenfive.dataway2.entity.ApiInfo;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description: API INFO 拦截器实现
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/30 12:11
 * @UpdateDate: 2020/6/30 12:11
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
public class DefaultApiInfoInterceptor extends ApiInfoInterceptor {

    public DefaultApiInfoInterceptor(QLRequestMappingFactory mappingFactory) {
        super(mappingFactory);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ApiInfo apiInfo) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView, ApiInfo apiInfo) {
        return;
    }
}
