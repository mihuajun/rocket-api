package com.github.alenfive.rocketapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DBConfig {

    private String name;
    private String comment;
    private String url;
    private String user;
    private String password;
    private String factory;

    private Map<String,Object> advanced;
}
