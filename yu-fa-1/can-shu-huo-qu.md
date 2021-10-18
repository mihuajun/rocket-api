# 参数获取

#### 1. 参数作用域

| 作用域        | 说明                                       |
| ---------- | ---------------------------------------- |
| `pathVar`  | URL路径传参,如`/book/{id}`                    |
| `param`    | URL传参，如`/book?id=1`                      |
| `body`     | BODY JSON格式传参，如`{"id":1}` ,或者form-data传参 |
| `header`   | HEADER参数,支持传入值为json串的对象（需要URLEncoder）    |
| `cookie`   | COOKIE参数                                 |
| `session`  | 会话数据获取，数据来源自request.getSession()的参数      |
| `bodyRoot` | 整个body Json 对象的引用,可配置  2.2.3.RELEASE新增   |
| `request`  | 当前请求的 HttpServletRequest                 |
| `response` | 当前请求的 HttpServletResponse                |

参数获取方式有两种： 1. 指定参数作用域：`param.id` 2. 忽略参数作用域：`id` ，忽略时将会从上到下每个变量域中逐个查询，一旦命中优先返回

**PATH参数获取**

```
log.info(id);
log.info(pathVar.id);
```

**URL参数获取**

```
log.info(type);
log.info(param.type);
```

**header参数获取**

```
log.info(sessionId);
log.info(header.sessionId);
```

**body参数获取**

```
log.info(user);
log.info(body.user.name);
```

**session参数获取**

```
log.info(session.name);
log.info(session);
```

#### 2.  当参数可能为空时取值方式为：

```
id= Utils.val("user.id")
```
