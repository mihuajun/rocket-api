# 导出csv,xls,xlsx



```
title = ["id":"编号","name":"姓名","mock":"test","createTime":"创建时间"]
list = db.find("select * from user");

//导出csv文件
return Utils.exportCsv("测试导出",title,list);

//xlsx导出
return  Utils.exportXlsx("测试导出",title,list);

//导出xls文件
return Utils.exportXls("测试导出",title,list);
```
