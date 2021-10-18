# 脚本引用

定义调用api接口GET /user/common如下：

```
log.info("print hello msg:{}",name)
 
def hello(){
    log.info("function hello..")
}
```

在其他（如：GET /user/list）脚本中引用 GET /user/common脚本如下：

```
name = "hello";
Utils.loadAPI("GET:/user/common")
hello()
```

将会打印出：

```
print hello msg:hello
function hello..
```
