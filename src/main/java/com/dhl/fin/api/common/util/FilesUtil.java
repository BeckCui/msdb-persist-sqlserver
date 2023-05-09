package com.dhl.fin.api.common.util;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by CuiJianbo on 2020.02.21.
 */
public class FilesUtil {

    /**
     * 清空目录下的所有文件
     *
     * @param path
     */
    public static void clearFile(String path) {
        FileUtil.clean(path);
    }

    /**
     * 删除文件或是目录
     *
     * @param path
     */
    public static void delete(String path) {
        delete(new File(path));
    }


    /**
     * 删除文件
     *
     * @param file
     */
    public static void delete(File file) {
        com.alibaba.excel.util.FileUtils.delete(file);
    }


    public static File getFile(String filePath) {
        return FileUtils.getFile(filePath);
    }

    /**
     * 循环获取子类文件
     *
     * @param filePath
     * @return
     */
    public static List<File> getFiles(String filePath) {
        File file = FileUtils.getFile(filePath);
        return getFiles(file);
    }

    public static List<File> getFiles(File file) {
        List<File> files = new LinkedList<>();
        if (file.isDirectory()) {
            File[] filesArray = file.listFiles();
            if (ArrayUtil.isNotEmpty(filesArray)) {
                for (File f : filesArray) {
                    files.addAll(getFiles(f));
                }
            }
        } else {
            files.add(file);
        }

        return files;
    }


    public static FileInputStream getFileInStream(String filePath) throws IOException {
        return FileUtils.openInputStream(getFile(filePath));
    }

    public static void createDir(String file) throws IOException {
        FileUtils.forceMkdir(getFile(file));
    }

    public static void writeFileToClient(File file, HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
        FileUtils.copyFile(file, response.getOutputStream());
    }

    public static void writeFileToClient(File file, HttpServletResponse response) throws IOException {
        writeFileToClient(file, response, file.getName());
    }

    public static void writeFileToLocal(File srcFile, File targetFile) throws IOException {
        FileUtils.copyFile(srcFile, targetFile);
    }

    public static InputStream getInputStreamFromRequest(MultipartHttpServletRequest request) throws IOException {
        Set<Map.Entry<String, MultipartFile>> fileSet = request.getFileMap().entrySet();
        if (fileSet.size() > 0) {
            for (Map.Entry<String, MultipartFile> fileEntry : fileSet) {
                MultipartFile file = fileEntry.getValue();
                return file.getInputStream();
            }
        }
        return null;
    }

    public static void copy(String srcFile, String targetFile) throws IOException {
        Path filePath = Paths.get(srcFile);
        Path targetPath = Paths.get(targetFile);
        CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
        Files.copy(filePath, targetPath, options);
    }

    public static void copy(File srcFile, File targetFile) throws IOException {
        Path filePath = srcFile.toPath();
        Path targetPath = targetFile.toPath();
        CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
        Files.copy(filePath, targetPath, options);
    }

}
