package com.github.alenfive.rocketapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.alenfive.rocketapi.entity.vo.LoginVo;
import com.github.alenfive.rocketapi.extend.IUserAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;

/**
 * 登录工具类
 */
@Component
public class LoginService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IUserAuthorization userAuthorization;

    private final static String rocketUserToken = "rocket-user-token";

    /**
     * 根据token 验证登录用户
     * @param request
     */
    public String getUser(HttpServletRequest request){
        String token = request.getHeader(rocketUserToken);
        return getUser(token);
    }
    public String getUser(String token){
        if (StringUtils.isEmpty(token)){
            return null;
        }
        try {
            LoginVo loginVo = objectMapper.readValue(Base64.getDecoder().decode(token), LoginVo.class);
            String user = userAuthorization.validate(loginVo.getUsername(), loginVo.getPassword());
            return user;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成token
     */
    public String getToken(LoginVo loginVo) throws JsonProcessingException {
        String user = userAuthorization.validate(loginVo.getUsername(), loginVo.getPassword());
        if (StringUtils.isEmpty(user)){
            return null;
        }
        String token = new String(Base64.getEncoder().encode(objectMapper.writeValueAsBytes(loginVo)));
        return token;
    }
}
