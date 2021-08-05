package com.github.alenfive.rocketapi.datasource;

import org.springframework.transaction.PlatformTransactionManager;

public interface DialectTransactionManager {
    public PlatformTransactionManager getTransactionManager();
}
