![输入图片说明](https://images.gitee.com/uploads/images/2020/0824/202855_aa972772_5139840.png "微信截图_20200824200716.png")

### 定位
拒绝CRUD。用尽可能简单的方式，完成尽可能多的需求。通过约定的方式 实现统一的标准。告别加班，拒绝重复劳动，远离搬砖

### 特性
1. 用于快速开发API接口。不再定义`Controller`,`Service`,`Dao`,`Mybatis`,`xml`,`Entity`,`VO`等对象和方法.
2. 可视化界面，将入参自动封装到可执行的脚本上，支持所有关系性数据库SQL执行语句，非关系型`MONGODB`查询语句.
3. 完全基于springboot2.x 作为springboot项目的stater方式集成,无侵入性，新老项目都能快速集成
4. 只需编写一行代码即可完成大部分的业务需求开发，使用难度级别（测试 or 运维）也可参与开发
5. 在线动态编译，无需重启，即时生效，多数据源操作
6. 版本控制,历史记录比对，回滚等功能
7. 远程一键发布到线上环境
8. 线上POSTMAN调试,保存POSTMAN信息或三方文档的自动生成，历史调用记录存储，回塑
9. 代码提示，SQL提示，语法提示
10. 用户管理控制，安全性控制，以及历史行为记录
11. 经过多次项目验证，传统业务型开发，服务端效率能够提升3-5倍，前后端联调提升效率1倍，测试效率2倍提升

### 工作原理
1.将API信息，请求方式，请求PATH，处理逻辑存储于数据库中，调用springboot提供的RequestMappingHandlerMapping.registerMapping/unregisterMapping 实现动态管理RequestMapping。  
2.依赖于java1.8提供的ScriptEngineManager方法，调用Groovy引擎，使代码逻辑能够实现动态编译，发布，而不用重启  
3.以springboot starter形式，集成在业务项目中


### 资源地址

>在线演示：http://39.98.181.90:8081/interface-ui    

>代码仓库：https://gitee.com/alenfive/rocket-api    

>文档地址: https://alenfive.gitbook.io/rocket-api/ 

>一分钟系列: https://blog.csdn.net/maple_son/article/details/108196584
  
### 项目预览  
![输入图片说明](https://images.gitee.com/uploads/images/2020/0922/162539_b59a4464_5139840.png "屏幕截图.png")
![输入图片说明](https://images.gitee.com/uploads/images/2020/0922/162711_70f6adb8_5139840.png "屏幕截图.png")


### 相关开源
Dataway https://www.hasor.net/doc/pages/viewpage.action?pageId=1573290  
Magic-API http://www.ssssssss.org/intro.html  
APIjson http://apijson.org/  
Graphql https://graphql.cn/ 

### 问题反馈 
微信号: freedom-Union  
邮件交流： kobe96688@126.com   
报告issue: https://github.com/alenfive/rocket-api/issues  
![输入图片说明](https://images.gitee.com/uploads/images/2020/0915/183440_93549b7f_5139840.png "屏幕截图.png")

### 一分钟快速项目集成  
https://alenfive.gitbook.io/rocket-api/fast-start

### FAQ
https://alenfive.gitbook.io/rocket-api/faq

## 来都来了，不点亮个星(Star)？




