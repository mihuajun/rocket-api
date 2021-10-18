# 纯SQL模式

作为一个美男子，姜少女，有时候就只想静静的写几条SQL，啥事不想干，v2.4.3.RELEASE提供了这样的方法，只有SQL,单纯的SQL

#### ### 设计思想

通过自动识别脚本中的内容，再加上约定的PATH后缀，实现判断是否是SQL脚本，分析SQL并执行相应的增，删，改，查，分页查询，计数查询，单条查询等数据库操作

#### 条件：

1. 脚本中只存在sql,连注释都不允许存在

案例：

####  1. 新增 - 只有sql,等于执行db.insert()

```
insert into user(id) values('123')
```

#### 2. 修改 - 只有sql ,等于执行db.update()

```
update user set name = #{name} where id = #{id}
```

####  3. 删除 - 只有sql 等于执行db.remove() 

```
delete from user where id = #{id}
```

#### 4. 列表查询 - 只有sql ,等于执行db.find()

```
select * from user
```

#### 5. 分页查询 - 分页查询是属于查询的一种，单纯从sql不能匹配是否应该分页,Rocket-API约定规则，如果PATH后缀定义为/page 将会执行自动分页，并以分页结构返回,等于执行db.pager()。脚本中依然只有sql

```
select * from user
```

#### 6. 返回第一条记录 - 单条记录返回也是属于查询的一种，所以同样从sql不能区分应该执行什么样的操作，所以同样以PATH后缀约定为/first，将会返回单体记录，以对象形式存在,等于执行db.findOne()。脚本中依然只有sql

```
select * from user limit 2
```

#### 7. 返回计数结果，同上。约定PATH以/count结尾，将会返回数量 等于执行db.count()操作，脚本中依然只有sql

```
select count(1) from user
```

#### 8. 约定的PATH后缀可配置如下：

```
spring: 
  rocket-api:
    sql-model:
      find-one-suffix: /first
      pager-suffix: /page
      count-suffix: /count
```
