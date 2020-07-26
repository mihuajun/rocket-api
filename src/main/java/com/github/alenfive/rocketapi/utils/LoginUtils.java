package com.github.alenfive.rocketapi.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录工具类
 */
public class LoginUtils {

    private final static String userIdent = "rocket-user";

    public static String getUser(HttpServletRequest request){
        Object user = request.getSession().getAttribute(userIdent);
        return user == null?null:user.toString();
    }

    public static void setUser(HttpServletRequest request, HttpServletResponse response, String user) {
        request.getSession().setAttribute(userIdent,user);
    }
}
