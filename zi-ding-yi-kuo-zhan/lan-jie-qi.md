# 自定义拦截器

继承抽象类:`com.github.alenfive.rocketapi.extend.ApiInfoInterceptor`

```
public class DefaultApiInfoInterceptor extends ApiInfoInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ApiInfo apiInfo) {
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView, ApiInfo apiInfo) {
        return;
    }
}
```

**注册拦截器方式一  ，继承WebMvcConfigurationSupport 会导致默认的资源配置无效，所以需要重新指定**

```
@Configuration
public class WebSupportConfig extends WebMvcConfigurationSupport {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/rocketapi/**")
                .addResourceLocations("classpath:/static/rocketapi/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DefaultApiInfoInterceptor());
    }
}
```

**注册方式二 （推荐）**

```
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new DefaultApiInfoInterceptor());
    }
}
```
