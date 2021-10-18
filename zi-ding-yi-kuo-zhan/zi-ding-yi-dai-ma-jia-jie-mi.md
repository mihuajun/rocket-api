# 自定义代码加解密

实现接口`com.github.alenfive.rocketapi.extend.IScriptEncrypt`

**默认实现类:**

```
/**
 * 代码加解密默认实现类
 */

@Component
public class DefaultScriptEncrypt implements IScriptEncrypt {

    //加密
    @Override
    public String encrypt(String source) throws Exception {
        return source;
    }
    //解密
    @Override
    public String decrypt(String encrypt) throws Exception {
        return encrypt;
    }
}
```
