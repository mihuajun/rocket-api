# 文件导入解析

#### csv解析返回结构为List>

```
result = Utils.parseCsv(files[0].getInputStream())
```

#### xls解析 返回结构为List\<Map\<String,String>>

```
Utils.parseXls(files[0].getInputStream())
```

#### xlsx 解析 返回结构为List\<Map\<String,String>>

```
Utils.parseXls(files[0].getInputStream())
```

#### 注意：excel操作，注意引入poi包

```
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>4.1.2</version>
</dependency>
```

![](<../.gitbook/assets/image (8).png>)
