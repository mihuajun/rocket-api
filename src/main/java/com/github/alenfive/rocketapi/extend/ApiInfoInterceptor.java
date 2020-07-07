package com.github.alenfive.rocketapi.extend;

/**
 * @Description: API INFO  拦截器
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/6/30 12:10
 * @UpdateDate: 2020/6/30 12:10
 * @UpdateRemark: init
 * @Version: 1.0
 */

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ApiInfoInterceptor implements HandlerInterceptor {

    private QLRequestMappingFactory mappingFactory;

    private ApiInfoInterceptor(){}

    public ApiInfoInterceptor(QLRequestMappingFactory mappingFactory){
        this.mappingFactory = mappingFactory;
    }

    private ApiInfo getApiInfo(HttpServletRequest request) throws Exception {
        String pattern = mappingFactory.buildPattern(request);
        String method = request.getMethod();
        return mappingFactory.getPathList(false).stream().filter(item->pattern.equals(item.getPath()) && (method.equals(item.getMethod()) || "ALL".equals(item.getMethod()))).findFirst().orElse(null);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiInfo apiInfo = getApiInfo(request);
        if (apiInfo == null)return true;
        return this.preHandle(request, response, handler,apiInfo);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        ApiInfo apiInfo = getApiInfo(request);
        if (apiInfo == null)return;
        this.postHandle(request, response, handler,modelAndView,apiInfo);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

    public abstract boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,ApiInfo apiInfo);

    public abstract void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,ModelAndView modelAndView,ApiInfo apiInfo);
}
