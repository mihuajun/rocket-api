# 文件上传下载预览

#### 文件上传

```
files[0].transferTo(new File("F:/"+fileName+".png"))
```

files为文件上传表单的key,服务端通过数组接收,以此来支持多文件上传

files的类型为:`org.springframework.web.multipart.MultipartFile`

![](<../.gitbook/assets/image (10).png>)

#### 文件下载

```
//本地文件下载
File file = new File("F:/hello.jpg");
Utils.download("hello.jpg",new FileInputStream(file))

//远程文件下载
URL url = new URL("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
Utils.download("hello.jpg",url.openConnection().getInputStream())
```

#### 文件图片预览

```
//本地图片预览
File file = new File("F:/hello.jpg");
Utils.preview(new FileInputStream(file))

//远程图片预览
URL url = new URL("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png");
Utils.preview(url.openConnection().getInputStream())
```
