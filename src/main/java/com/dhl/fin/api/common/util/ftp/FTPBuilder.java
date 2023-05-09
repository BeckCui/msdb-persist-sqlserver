package com.dhl.fin.api.common.util.ftp;

import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 8/26/2020
 */
@Setter
@Getter
public class FTPBuilder {

    private SSHClient sshClient;

    private String path;

    FTPBuilder(String sftpConfigPrefix) throws IOException {
        String host = SpringContextUtil.getPropertiesValue(sftpConfigPrefix + "." + "host");
        String port = SpringContextUtil.getPropertiesValue(sftpConfigPrefix + "." + "port");
        String userName = SpringContextUtil.getPropertiesValue(sftpConfigPrefix + "." + "userName");
        String passWord = SpringContextUtil.getPropertiesValue(sftpConfigPrefix + "." + "passWord");
        path = SpringContextUtil.getPropertiesValue(sftpConfigPrefix + "." + "path");
        path = StringUtil.isEmpty(path) ? "" : path.trim();

        int intPort = StringUtil.isEmpty(port) ? 22 : Integer.valueOf(port);

        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(host, intPort);
        client.authPassword(userName, passWord);
        this.sshClient = client;

    }

    /**
     * 从sftp下载到本地
     *
     * @param remoteFile
     * @param localFile
     */
    public void downLoadToLocal(String remoteFile, String localFile) {
        try {
            SFTPClient sftpClient = sshClient.newSFTPClient();

            remoteFile = dealFilePath(remoteFile);
            remoteFile = StringUtil.join(path.trim(), "/", remoteFile.trim());
            sftpClient.get(remoteFile, localFile);

            sftpClient.close();
            sshClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFileNameList(String dir) throws IOException {
        SFTPClient sftpClient = sshClient.newSFTPClient();

        String dictionary = StringUtil.join(path.trim(), "/", dir);

        return sftpClient.ls(dictionary)
                .stream()
                .map(p -> p.isDirectory() ? "" : p.getName())
                .filter(StringUtil::isNotEmpty)
                .collect(Collectors.toList());
    }


    private String dealFilePath(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return "/";
        }
        char s = filePath.charAt(0);
        if (s == '/' || s == '\\') {
            return filePath.substring(1);
        }
        return filePath;
    }

    /**
     * 把本地文件上传到ftp
     *
     * @param remoteFile
     * @param localFile
     */
    public void uploadFromLocal(String remoteFile, String localFile) throws IOException {
        SFTPClient sftpClient = sshClient.newSFTPClient();

        remoteFile = dealFilePath(remoteFile);
        remoteFile = StringUtil.join(path.trim(), "/", remoteFile.trim());
        sftpClient.put(localFile, remoteFile);

        sftpClient.close();
        sshClient.disconnect();
    }


    /**
     * 从sftp下载后传送到客户端
     *
     * @param remoteFile
     * @param response
     */
    public void downLoadToClient(String remoteFile, HttpServletResponse response) {
        try {
            remoteFile = remoteFile.replace(path, "");
            String[] tempFiles = remoteFile.split("/");
            String fileName = tempFiles[tempFiles.length - 1];
            fileName = fileName.replace("\\", "");
            fileName = fileName.replace("/", "");
            String uploadPath = SpringContextUtil.getPropertiesValue("custom.uploadPath");
            uploadPath = StringUtil.isEmpty(uploadPath) ? "C://temp" : uploadPath;
            String filePath = uploadPath + File.separator + fileName;
            downLoadToLocal(remoteFile, filePath);
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
            File targetFile = FileUtils.getFile(filePath);
            FileUtils.copyFile(targetFile, response.getOutputStream());
            FileUtils.forceDelete(targetFile);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 删除文件
     *
     * @param file
     * @throws IOException
     */
    public void deleteFile(String file) throws IOException {
        if (StringUtil.isNotEmpty(file)) {
            file = path + File.separator + file;
            SFTPClient sftpClient = sshClient.newSFTPClient();
            sftpClient.rm(file);
        }
    }


    /**
     * 删除文件夹
     *
     * @param dir
     * @throws IOException
     */
    public void deleteDir(String dir) throws IOException {
        if (StringUtil.isNotEmpty(dir)) {
            dir = path + File.separator + dir;
            SFTPClient sftpClient = sshClient.newSFTPClient();
            sftpClient.rmdir(dir);
        }
    }

    /**
     * 创建文件夹
     *
     * @param dirPath
     * @throws IOException
     */
    public void createDir(String dirPath) throws IOException {
        if (StringUtil.isNotEmpty(dirPath)) {
            SFTPClient sftpClient = sshClient.newSFTPClient();
            dirPath = path + File.separator + dirPath;
            sftpClient.mkdirs(dirPath);
        }
    }


}
