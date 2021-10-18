# 脚本参数映射



通过`db.`方法调用的入参,类mybatis语法

**参数来源于HTTP入参，和执行脚本的上下文,当变量名重复时，优先上下文变量,就近获取**

> \#{varName} :自动识别参数类型,int,string,list进行处理
>
> ${varName} :原始状态输出，会导致sql注入问题，用于入参为关键字
>
> ?{condition,express} :对condition条件进行判断，如果值为true，则将express部分保留，否则丢弃。`condition`值为, `' '` , `NULL` ,` [ ]` 空时，将得到false。condition部份允许接收完整的groovy语法

#### 普通参数

```
sql = """
select * from user where name = #{name}
"""
db.find(sql)
```

#### 对象参数

```
sql = """
select * from user where name = #{user.name}
"""
```

#### 数组参数,会自动转换为逗号分隔的对象

```
sql = """
select * from user where id = #{idList[0]}
"""
```

```
sql = """
select * from user where id in (#{idList})
"""
```

#### 关键字参数

```
sql = """
select * from ${table}
"""
```

#### 非空参数判断,当`nameVar`参数为空时后面的`and name = #{nameVar}`会被忽略，类似于mybatis if语法

```
sql = """
select * from user where 1=1 ?{nameVar,and name = #{nameVar}}
"""
```

#### 逻辑表达式 mybatis if语法判断 -- 是groovy语法就行

```
nameVar = "123"
sql = """
select * from user where 1=1 ?{nameVar == "123",and name = #{nameVar}}
"""
```

#### 函数式 mybatis if 语法判断--是groovy语法就行

```
def validate(name){
    return name == "123";
}

nameVar = "123"

sql = """
select * from user where 1=1 ?{validate(nameVar),and name = #{nameVar}}
"""
```
