package com.github.alenfive.rocketapi.entity.vo;

import lombok.Data;

/**
 * 登录入参实体
 */
@Data
public class LoginReq {
    private String username;
    private String password;
}
