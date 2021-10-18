# sqlserver



```
CREATE TABLE api_info (
    id varchar(45) NOT NULL,
    method varchar(45) DEFAULT NULL,
    path varchar(100) DEFAULT NULL,
    type varchar(5) DEFAULT NULL ,
    service varchar(45) DEFAULT NULL,
    group_name varchar(45) DEFAULT NULL,
    editor varchar(45) DEFAULT NULL,
    name varchar(200) DEFAULT NULL,
    datasource varchar(45) DEFAULT NULL,
    script text,
    options text,
    create_time varchar(45) NULL DEFAULT NULL,
    update_time varchar(45) NULL DEFAULT NULL,
    PRIMARY KEY (id),
    ) ;

CREATE TABLE api_example (
    id varchar(45) NOT NULL,
    api_info_id varchar(45) NOT NULL,
    method varchar(45) DEFAULT NULL,
    url text,
    request_header text,
    request_body text,
    response_header text,
    response_body text,
    status varchar(10) DEFAULT NULL,
    elapsed_time int DEFAULT NULL,
    options text,
    editor varchar(45) DEFAULT NULL,
    create_time varchar(45) DEFAULT NULL,
    PRIMARY KEY (id),
    ) ;

CREATE TABLE api_info_history (
    id varchar(45) NOT NULL,
    api_info_id varchar(45) NOT NULL,
    method varchar(45) DEFAULT NULL,
    path varchar(100) DEFAULT NULL,
    type varchar(5) DEFAULT NULL,
    service varchar(45) DEFAULT NULL,
    group_name varchar(45) DEFAULT NULL,
    editor varchar(45) DEFAULT NULL,
    name varchar(200) DEFAULT NULL,
    datasource varchar(45) DEFAULT NULL,
    script text,
    options text,
    create_time varchar(45) NULL DEFAULT NULL,
    PRIMARY KEY (id)
    );


-- 开启spring.rocket-api.config-enabled: true 才需要此表
CREATE TABLE api_config (
    id varchar(45) NOT NULL,
    service varchar(45) NOT NULL,
    config_context text,
    PRIMARY KEY (id),
    ) ;
```
