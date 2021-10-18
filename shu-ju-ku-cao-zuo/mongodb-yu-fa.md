# MongoDb专区

语法为标准的mongodb command语法，见： [https://docs.mongodb.com/manual/reference/command/](https://docs.mongodb.com/manual/reference/command/)

**新增**

```
str = """
{
  "insert":"book",
  "documents":[
    {
      "name":"富贵",
      "price":38.8
    }]
}
"""
return db.insert(str)
```

**列表查询**

```
sql = """
{
    "find":"book",
    "filter":{
        "name":#{name}
    }
}
"""
return db.find(sql);
```

**单条查询**

```
sql = """
{
    "find":"book",
    "filter":{
        "name":#{name}
    }
}
"""
return db.findOne(sql);
```

**计数查询**

```
count = """
{
  count: "book",
  query: {
        "name":#{name}
    }
}
"""
return db.findOne(sql);
```

**分页查询**

```
sql = """
{
    "find":"book",
    "filter":{
        "name":#{name}
    }
}
"""
return db.pager(sql);
```
