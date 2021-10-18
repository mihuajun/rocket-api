# 数据库操作语法

```
//数量统计，返回Long
return db.count("select count(1) from user");

//返回命中的第一条记录，MAP类型
return db.findOne("select * from user where id = #{id}");

//返回命中的所有记录，返回LIST
return db.find("select * from user");

//插入 返回主键ID,Object类型
return db.insert("insert into user(name) values(#{name})");

//更新 返回更新的数量
return db.update("update user set name=#{name} where id = #{id}");

//删除 返回删除的数量
sql = """
 delete from user
 where id = #{id}
"""
return db.remove(sql);

//自动分页
return db.pager("select * from user")

//操作指定数据源
return db.findOne("select * from user where id = #{id}","mysql");
```
