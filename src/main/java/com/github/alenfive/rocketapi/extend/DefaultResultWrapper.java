package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ResultWrapper;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
