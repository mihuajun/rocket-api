package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Properties;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBConfig extends ApiEntity{

    private String factory;
    private String name;
    private String comment;

    private String url;
    private String user;
    private String password;
    private Properties properties;
}
