package com.github.alenfive.rocketapi.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    /*public static List<String> getClasses(String packageName){
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName))));
        return reflections.getSubTypesOf(Object.class).stream().map(item->item.getName()).collect(Collectors.toList());
    }*/

    public static List<String> scan() throws URISyntaxException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Set<String> classes = new HashSet<>();
        do{
            if(loader instanceof URLClassLoader){
                classes.addAll(scan(((URLClassLoader) loader).getURLs()));
            }
        }while ((loader = loader.getParent()) != null);
        //classes.addAll(scan(Launcher.getBootstrapClassPath().getURLs()));
        return new ArrayList<>(classes);
    }

    private static Set<String> scan(URL[] urls) throws URISyntaxException {
        Set<String> classes = new HashSet<>();
        if(urls != null){
            for (URL url : urls) {
                String protocol = url.getProtocol();
                if("file".equalsIgnoreCase(protocol)){
                    String path = url.getPath();
                    if (path.toLowerCase().endsWith(".jar")) {
                        classes.addAll(scanJarFile(url));
                    } else {
                        classes.addAll(scanDirectory(new File(url.toURI()), null));
                    }
                }else if("jar".equalsIgnoreCase(protocol)){
                    classes.addAll(scanJarFile(url));
                }
            }
        }
        return classes;
    }

    private static List<String> scanDirectory(File dir, String packageName) {
        File[] files = dir.listFiles();
        List<String> classes = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (file.isDirectory()) {
                    classes.addAll(scanDirectory(file, packageName == null ? name : packageName + "." + name));
                } else if (name.endsWith(".class") && !name.contains("$")) {
                    classes.add(filterFullName(packageName + "." + name.substring(0, name.length() - 6)));
                }
            }
        }
        return classes;
    }

    private static String filterFullName(String fullName){
        if(fullName.startsWith("BOOT-INF.classes.")){
            fullName = fullName.substring(17);
        }
        return fullName;
    }

    private static List<String> scanJarFile(URL url) {
        List<String> classes = new ArrayList<>();
        try(ZipInputStream zis = new ZipInputStream(url.openStream())){
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().contains("META-INF")) {
                    String className = entry.getName();
                    if (className.endsWith(".class") && !className.contains("$")) {
                        classes.add(filterFullName(className.substring(0, className.length() - 6).replace("/", ".")));
                    }
                }
            }
        }catch (IOException ignored){

        }
        return classes;
    }
}