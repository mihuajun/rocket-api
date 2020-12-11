package com.github.alenfive.rocketapi.extend;

/**
 * @Description:
 * @Copyright: Copyright (c) 2019  ALL RIGHTS RESERVED.
 * @Company: 成都国盛天丰技术有限责任公司
 * @Author: 米华军
 * @CreateDate: 2020/12/10 11:52
 * @UpdateDate: 2020/12/10 11:52
 * @UpdateRemark: init
 * @Version: 1.0
 * @menu
 */
public interface ISQLInterceptor {
    String before(String script);

    void after(String script);
}
