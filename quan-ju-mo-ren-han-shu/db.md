---
description: 数据库操作函数
---

# db

com.github.alenfive.rocketapi.function.DbFunction

**db.count(script,\[datasource])**

返回数量，计数查询

**db.findOne(script,\[datasource])**

单条记录查询，返回map

**db.find(script,\[datasource])**

列表查询，返回ArrayList\<map>

**db.insert(script,\[datasource])**

新增，返回自增主键，无值返回空

**db.remove(script,\[datasource])**

删除，返回删除的数量

**db.update(script,\[datasource])**

更新，返回修改数量

**db.pager(script,\[datasource])**

自动分页查询，更多[自动查询查看](../zi-ding-yi-kuo-zhan/fen-ye-cha-xun.md)



db函数返回字段规则，会自动进行驼峰转换：

| 例子      | 输出     |
| ------- | ------ |
| user_id | userId |
| USER_ID | userId |
| userId  | userId |

规则描述：

1. 当字段名为全大写时，会转换为小写驼峰 例: USER_ID -> userId
2. 当字段中带有下划线时，会转换为驼峰 例: user_id -> userId
3. 其他格式字段原样输出 例：userId -> userId
