package com.github.alenfive.rocketapi.utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 包工具类
 */
public class PackageUtil {

    public static List<Class> loadClassByLoader(ClassLoader load) throws Exception{
        Enumeration<URL> urls = load.getResources("");
        //放所有类型
        List<Class> classes = new ArrayList<Class>();
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            //文件类型（其实是文件夹）
            if (url.getProtocol().equals("file")) {
                loadClassByPath(null, url.getPath(), classes, load);
            }
        }
        return classes;
    }

    public static void loadClassByPath(String root, String path, List<Class> list, ClassLoader load) {
        File f = new File(path);
        if(root==null) root = f.getPath();
        //判断是否是class文件
        if (f.isFile() && f.getName().matches("^.*\\.class$")) {
            try {
                String classPath = f.getPath();
                //截取出className 将路径分割符替换为.（windows是\ linux、mac是/）
                String className = classPath.substring(root.length()+1,classPath.length()-6).replace('/','.').replace('\\','.');
                list.add(load.loadClass(className));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            File[] fs = f.listFiles();
            if (fs == null) return;
            for (File file : fs) {
                loadClassByPath(root,file.getPath(), list, load);
            }
        }
    }
}