# 返回



**默认返回：**

```
"hello"  //最后一行
```

**申明式返回：**

```
return "hello"
```

**忽略返回体封装：**

```
return Utils.ignoreWrapper("hello")
```

> 返回体封装结构见：“[自定义返回结构封装](../zi-ding-yi-kuo-zhan/zi-ding-yi-fan-hui-jie-gou-feng-zhuang.md)”

**异步返回：**

```
Thread.start {
    Thread.sleep(10000)
    log.info("async msg ...")
}
return "yes";
```
