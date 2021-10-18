# 参数验证，断言判断

```
//正则断言
Assert.regex("^\\d+$",phone,"非手机号");

//逻辑断言
Assert.isTrue(true,"逻辑不匹配")

//非空断言
Assert.isNotEmpty(name,"参数为空")

//为空断言
Assert.isEmpty("","参数非空")

//匹配断言
Assert.equals("a","a","参数不足")
```

默认抛出`RuntimeException.class`异常 `DefaultAssertException.class` 自定义异常通过实现接口： `com.github.alenfive.rocketapi.extend.IAssertException`

```
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
