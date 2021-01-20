package com.github.alenfive.rocketapi.extend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 执行结果返回
 */

public interface IResultWrapper {
    public Object wrapper(Object data, HttpServletRequest request, HttpServletResponse response);
    public Object throwable(Throwable throwable,HttpServletRequest request, HttpServletResponse response);
}
