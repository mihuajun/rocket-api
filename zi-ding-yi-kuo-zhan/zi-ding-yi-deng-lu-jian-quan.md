# 自定义登录鉴权

> 默认情况下，任意账号都可以登录成功
>
> 自定义登录，需要实现接口`com.github.alenfive.rocketapi.extend.IUserAuthorization`

#### 默认实现：

```
@Component
public class DefaultUserAuthorization implements IUserAuthorization {

    @Override
    public String validate(String username, String password) {
        return StringUtils.isEmpty(username)?"admin":username;
    }
}
```
