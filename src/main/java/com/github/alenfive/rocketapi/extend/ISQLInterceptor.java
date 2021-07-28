package com.github.alenfive.rocketapi.extend;

public interface ISQLInterceptor {
    String before(String script);

    void after(String script);
}
