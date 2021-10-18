# 概述

**工作原理如下**

如何实现的动态注册RequestMapping？ 

1. 使用springboot 提供的 `RequestMappingHandlerMapping`  来动态的增加和移除`requestMapping`,与通过代码方式在`Controller` 中定义的 `@RequestMapping` 注册mapping一致，也归于springboot mapping管理

在Rocket-API中编辑的代码是如何运行的，效率怎么样？

1. 底层使用java1.8以后提供的 `ScriptEngineManager` 的 `groovy`引擎，来实现动态编译，在首次访问后会生成 java class 类，之后就是纯调用，不再进行编译

