package com.dhl.fin.api.common.util.ftp;


import java.io.IOException;

/**
 * @author becui
 * @date 5/5/2020
 */
public class FTPUtil {

    FTPBuilder ftpBuilder;

    /**
     * 创建FTP build工具
     *
     * @param sftpConfigPrefix 配置文件参数前缀
     * @return
     * @throws IOException
     */
    public static FTPBuilder build(String sftpConfigPrefix) throws IOException {
        FTPBuilder ftpBuilder = new FTPBuilder(sftpConfigPrefix);
        return ftpBuilder;
    }

    /**
     * 创建FTP build工具
     * 默认的以 custom.ftp 为前缀的FTP配置
     *
     * @return
     * @throws IOException
     */
    public static FTPBuilder build() throws IOException {
        FTPBuilder ftpBuilder = new FTPBuilder("custom.ftp");
        return ftpBuilder;
    }


}





