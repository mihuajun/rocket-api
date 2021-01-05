package com.github.alenfive.rocketapi.extend;

/**
 * 用户授权接口
 */
public interface IUserAuthorization {
    public String validate(String username, String password);
}
