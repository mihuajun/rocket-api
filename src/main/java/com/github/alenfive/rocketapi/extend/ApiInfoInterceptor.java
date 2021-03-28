package com.github.alenfive.rocketapi.extend;

/**
 * API INFO  拦截器抽象接口类，可获取 apiInfo相关信息
 */

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.config.SpringContextUtils;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ApiInfoInterceptor implements HandlerInterceptor {

    private ApiInfo getApiInfo(HttpServletRequest request) throws Exception {
        QLRequestMappingFactory mappingFactory = SpringContextUtils.getApplicationContext().getBean(QLRequestMappingFactory.class);
        String pattern = mappingFactory.buildPattern(request);
        if (pattern == null){
            return null;
        }
        String method = request.getMethod();
        return mappingFactory.getPathList(false).stream().filter(item->pattern.equals(item.getFullPath()) && (method.equals(item.getMethod()) || "ALL".equals(item.getMethod()))).findFirst().orElse(null);
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
