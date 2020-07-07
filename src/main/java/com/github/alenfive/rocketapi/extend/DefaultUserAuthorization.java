package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @Description: 默认用户授权
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Author: 米华军
 * @CreateDate: 2020/7/2 20:29
 * @UpdateDate: 2020/7/2 20:29
 * @UpdateRemark: init
 * @Version: 1.0
 */

@Component
public class DefaultUserAuthorization implements IUserAuthorization {

    @Override
    public String validate(String username, String password) {
        return StringUtils.isEmpty(username)?"admin":username;
    }
}
