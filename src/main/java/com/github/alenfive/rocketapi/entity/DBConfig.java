package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DBConfig extends ApiEntity{

    private String driver;
    private String name;
    private String comment;

    private String url;
    private String user;
    private String password;

    private boolean enabled;

    private Properties properties;
}
