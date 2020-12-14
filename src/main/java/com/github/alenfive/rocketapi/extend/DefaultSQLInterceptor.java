package com.github.alenfive.rocketapi.extend;

import org.springframework.stereotype.Component;

@Component
public class DefaultSQLInterceptor implements ISQLInterceptor {

    @Override
    public String before(String script) {
        return script;
    }

    @Override
    public void after(String script) {
    }
}
