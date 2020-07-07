package com.github.alenfive.dataway2.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/7/2 21:59
 * @UpdateDate: 2020/7/2 21:59
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 登录工具类
 */
public class LoginUtils {

    private final static String userIdent = "user";

    public static String getUser(HttpServletRequest request){
        Object user = request.getSession().getAttribute(userIdent);
        return user == null?null:user.toString();
    }

    public static void setUser(HttpServletRequest request, String user) {
        request.getSession().setAttribute(userIdent,user);
    }
}
