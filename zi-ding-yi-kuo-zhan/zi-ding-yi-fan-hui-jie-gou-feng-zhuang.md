# 自定义返回结构封装

**实现接口com.github.alenfive.rocketapi.extend.IResultWrapper**

```
/**
 * 默认结果包装类
 */
@Component
public class DefaultResultWrapper  implements IResultWrapper{

    @Override
    public Object wrapper(Object data, HttpServletRequest request, HttpServletResponse response) {
        return new ResultWrapper("0",request.getRequestURI(),"succeed",data);
    }

    @Override
    public Object throwable(Throwable throwable, HttpServletRequest request, HttpServletResponse response) {
        return new ResultWrapper("500",request.getRequestURI(),throwable.getMessage(),null);
    }
}

```

`ResultWrapper`类可自行定义
