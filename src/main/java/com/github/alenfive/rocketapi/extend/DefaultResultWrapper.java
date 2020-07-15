package com.github.alenfive.rocketapi.extend;

import com.github.alenfive.rocketapi.entity.ResultWrapper;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/7/15 17:11
 * @UpdateDate: 2020/7/15 17:11
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu 默认结果包装类
 */
@Component
public class DefaultResultWrapper  implements IResultWrapper{

    @Override
    public Object wrapper(String code, String msg, Object data, HttpServletRequest request, HttpServletResponse response) {
        return new ResultWrapper(code,request.getRequestURI(),msg,data);
    }
}
