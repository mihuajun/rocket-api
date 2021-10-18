# 基本语法

```java
//数组定义方式
return ["123","456"];

//对象定义方式
return ["id":123,"name":"五"];

//变量定义
def sql = "select * from user";

//多行换行文本 """
sql = """
    select * from user 
    where name = #{name}
"""

//循环
for(item in list){  

}
for(i=0;i<10;i++)

}

//逻辑判断
if(1<2){

}

if(condition){ 

}else{   

}
```

更多语法见groovy语法：[https://www.w3cschool.cn/groovy/groovy_overview.html](https://www.w3cschool.cn/groovy/groovy_overview.html)
