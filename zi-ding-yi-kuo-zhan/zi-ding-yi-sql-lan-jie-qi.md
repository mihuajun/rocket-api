# 自定义SQL拦截器

实现接口 \`com.github.alenfive.rocketapi.extend.ISQLInterceptor\`

```
@Component
public class DefaultSQLInterceptor implements ISQLInterceptor {

    @Override
    public String before(String script) {
        return script;
    }

    @Override
    public void after(String script) {
    }
}

```
