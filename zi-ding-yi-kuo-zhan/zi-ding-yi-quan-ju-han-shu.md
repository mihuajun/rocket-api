# 自定义全局函数

自定义全局函数通过实现接口`com.github.alenfive.rocketapi.function.IFunction`

```
@Component
public class EnvFunction implements IFunction{

    @Autowired
    private Environment environment;

    @Override
    public String getVarName() {
        return "env";
    }

    public String get(String key){
        return environment.getProperty(key);
    }

    public String get(String key,String defaultValue){
        return environment.getProperty(key,defaultValue);
    }
}
```

函数调用

```
def appName = env.get("spring.application.name")
```
