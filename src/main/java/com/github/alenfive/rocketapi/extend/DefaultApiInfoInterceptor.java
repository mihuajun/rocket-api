package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.config.QLRequestMappingFactory;
import com.github.alenfive.rocketapi.entity.ApiInfo;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API INFO 拦截器实现
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
