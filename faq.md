# FAQ

**1.  如何切换编辑模式与POSTMAN模式**

1. 使用快捷键 alt + w 可切换模式
2. 页面按钮触发

![](<.gitbook/assets/image (15).png>)

**2. 如何触发SQL代码提示**

1.  使用md描述结构"""sql,在其中编辑会有相关提示

    ```
    """sql
    select * from user
    """
    ```

**3. RUN,DEBUG时运行参数来自哪里**

1. 参数来自于页面POSTMAN模式下的参数

**4. db.pager() 自动分页，为什么无效？**

1. 目前自动分页默认只支持，mysql,oracle,mongodb,sqlserver,db2,postgres 如需其他数据源，可自行扩展，参考类 `OracleDataSource`,欢迎PR

**5.为什么访问/interface-ui时页面异常？**

1. 看看你的项目中是否有对静态资源做路径修改，默认静态资源放在resource/static/rocketapi下
2. 能力有限，只有chrom浏览器全面支持，其他浏览器未经过验证

**6. 为什么会提示重复注册BEAN**

1. 项目是基于springboot starter开发，该依赖包不需要手动执行 scan bean操作

**7. 如何集成swagger**

1. 理论上不需要做任何修改,就能扫描到所有Rocket-API定义的接口
2.  注意在定义swagger配置对象时指定如下any()

    ```
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any()) //所有
                .paths(PathSelectors.any())            //所有
                .build();
    ```
