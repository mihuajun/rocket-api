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
    public Object wrapper(String code, String msg, Object data, HttpServletRequest request, HttpServletResponse response) {
        return new ResultWrapper(code,request.getRequestURI(),msg,data);
    }
}
