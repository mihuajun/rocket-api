package com.github.alenfive.rocketapi.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录入参实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVo {
    private String username;
    private String password;
}
