package com.github.alenfive.rocketapi.extend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 执行结果返回
 */

public interface IResultWrapper {
    public Object wrapper(String code, String msg, Object data, HttpServletRequest request, HttpServletResponse response);
}
