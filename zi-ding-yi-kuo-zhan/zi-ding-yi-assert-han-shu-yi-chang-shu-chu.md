# 自定义Assert函数异常输出

实现接口 `com.github.alenfive.rocketapi.extend.IAssertException`

```
/**
 * 默认参数验证异常处理类
 */
@Component
public class DefaultAssertException implements IAssertException {

    @Override
    public void exception(String... input) {
        if (input.length != 1){
            throw new RuntimeException("Parameter length mismatch" + input.toString());
        }
        throw new RuntimeException(input[0]);
    }
}
```
