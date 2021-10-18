# mysql



```sql
CREATE TABLE `api_info` (
  `id` varchar(45) NOT NULL,
  `method` varchar(45) DEFAULT NULL,
  `path` varchar(100) DEFAULT NULL,
  `type` varchar(5) DEFAULT NULL COMMENT '类型：CODE,QL',
  `service` varchar(45) DEFAULT NULL,
  `editor` varchar(45) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `datasource` varchar(45) DEFAULT NULL,
  `script` text,
  `options` text,
  `create_time` varchar(45) DEFAULT NULL,
  `full_path` varchar(200) DEFAULT NULL,
  `directory_id` varchar(45) DEFAULT NULL,
  `update_time` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_path_method` (`service`,`full_path`,`method`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路径明细';


CREATE TABLE `api_info_history` (
  `id` varchar(45) NOT NULL,
  `api_info_id` varchar(45) NOT NULL,
  `method` varchar(45) DEFAULT NULL,
  `path` varchar(100) DEFAULT NULL,
  `type` varchar(5) DEFAULT NULL COMMENT '类型：CODE,QL',
  `service` varchar(45) DEFAULT NULL,
  `editor` varchar(45) DEFAULT NULL,
  `name` varchar(200) DEFAULT NULL,
  `datasource` varchar(45) DEFAULT NULL,
  `script` text,
  `options` text,
  `create_time` varchar(45) DEFAULT NULL,
  `full_path` varchar(200) DEFAULT NULL,
  `directory_id` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路径明细历史';


CREATE TABLE `api_example` (
  `id` varchar(45) NOT NULL,
  `api_info_id` varchar(45) NOT NULL,
  `method` varchar(45) DEFAULT NULL,
  `url` text,
  `request_header` text,
  `request_body` text,
  `response_header` text,
  `response_body` text,
  `status` varchar(10) DEFAULT NULL,
  `elapsed_time` int(11) DEFAULT NULL,
  `options` text,
  `editor` varchar(45) DEFAULT NULL,
  `create_time` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_api_id` (`api_info_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟数据';

//在开启了spring.rocket-api.config-enabled=true才需要
CREATE TABLE `api_config` (
  `id` varchar(45) NOT NULL,
  `service` varchar(45) NOT NULL,
  `config_context` text,
  `type` VARCHAR(45) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `api_directory` (
  `id` varchar(45) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `path` varchar(200) DEFAULT NULL,
  `parent_id` varchar(45) DEFAULT NULL,
  `service` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

```
