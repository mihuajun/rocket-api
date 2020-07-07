package com.github.alenfive.dataway2.extend;

/**
 * @Description: 用户授权接口
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/7/2 20:28
 * @UpdateDate: 2020/7/2 20:28
 * @UpdateRemark: init
 * @Version: 1.0
 */
public interface IUserAuthorization {
    public String validate(String username,String password);
}
