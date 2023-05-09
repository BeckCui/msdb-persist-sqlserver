package com.dhl.fin.api.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ReflectUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by CuiJianbo on 2020.02.21.
 */
public class ObjectUtil {


    public static Boolean notNull(Object o) {
        return ObjectUtils.notEqual(o, null);
    }

    public static Boolean isNull(Object o) {
        return !ObjectUtils.notEqual(o, null);
    }

    public static Class loadClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * List为空和 null的字段不复制
     *
     * @param srcObject
     * @param targetObject
     */
    public static void copyFieldValue(Object srcObject, Object targetObject) {
        BeanUtil.copyProperties(srcObject, targetObject, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));

        Arrays.stream(targetObject.getClass().getDeclaredFields())
                .filter(p -> p.getType().equals(List.class))
                .forEach(p -> {
                    Object o = ReflectUtil.getFieldValue(targetObject, p);
                    if (ObjectUtil.notNull(o) && ((List) o).size() == 0) {
                        ReflectUtil.setFieldValue(targetObject, p, null);
                    }
                });
    }


    /**
     * 从包package中获取所有的Class
     *
     * @return
     */
    public static List<Class> getClasses(String packageName) {

        List<Class> classes = new ArrayList<>();
        String packageDirName = packageName.replace('.', '/');
        Enumeration<URL> dirs;
        try {

            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    File dir = new File(filePath);
                    List<File> allFiles = FilesUtil.getFiles(dir);
                    for (File f : allFiles) {
                        Class enumClass = null;
                        String path = f.getPath();
                        String className = path.substring(path.indexOf("com\\dhl\\fin\\api")).replaceAll("\\\\", ".");
                        if (className.endsWith("class")) {
                            className = className.replaceAll("\\.class", "");
                        } else {
                            continue;
                        }
                        try {
                            enumClass = Thread.currentThread().getContextClassLoader().loadClass(className);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (ObjectUtil.notNull(enumClass)) {
                            classes.add(enumClass);
                        }
                    }
                } else if ("jar".equals(protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    findClassesByJar(packageName, jar, classes);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    private static void findClassesByJar(String pkgName, JarFile jar, List<Class> classes) {
        String pkgDir = pkgName.replace(".", "/");
        Enumeration<JarEntry> entry = jar.entries();

        JarEntry jarEntry;
        String name, className;
        Class<?> claze;
        while (entry.hasMoreElements()) {
            jarEntry = entry.nextElement();
            name = jarEntry.getName();
            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }

            if (jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith(".class")) {
                continue;
            }
            className = name.substring(0, name.length() - 6);

            claze = ObjectUtil.loadClass(className.replace("/", "."));

            if (claze != null) {
                classes.add(claze);
            }
        }
    }

}
