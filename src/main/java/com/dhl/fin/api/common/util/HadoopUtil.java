package com.dhl.fin.api.common.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dhl.fin.api.common.exception.ConnectionException;
import com.dhl.fin.api.common.service.RedisService;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author becui
 * @date 8/21/2020
 */
public class HadoopUtil {

    private static final String TOKEN_KEY = "Aa0WK9J7U0gqjhwc5DdKv7S686K4vu53Mjwm6mGbOYw=";
    private static final String FILE_DIR = "caceinvoice";

    /**
     * @param file     文件
     * @param dir      hadoop存放目录
     * @param fileName hadoop存放文件名字
     */
    public void upload(File file, String dir, String fileName) {
        byte[] filebyte = FileUtil.readBytes(file);
        upload(filebyte, dir, fileName);
    }

    private void upload(byte[] filebyte, String dir, String fileName) {
        String hadoopSocket = getHadoopSocket();
        String result = "";
        try {
            result = HttpRequest
                    .put("http://" + hadoopSocket + "/services/file/put/caceinvoice?filename=" + dir + File.separator + fileName)
                    .body(filebyte)
                    .header("Content-Type", "application/octet-stream")
                    .execute().body();

            if (!result.equalsIgnoreCase("{result:\"OK\"}")) {
                throw new ConnectionException("上传文件到hadoop失败," + result);
            }
        } catch (Exception e) {
            throw new ConnectionException("hadoop连接失败");
        }

    }


    /**
     * 下载并发送到Browser端
     */
    public static void downloadToClient(String dir, String fileName, HttpServletResponse response) throws IOException {


        byte[] fileBytes = getFileFromHadoop(dir, fileName);

        String uploadPath = SpringContextUtil.getPropertiesValue("custom.uploadPath");
        String file = uploadPath + File.separator + "hadoop" + File.separator + fileName;
        FileUtil.writeBytes(fileBytes, uploadPath);
        File tempFile = new File(file);
        FilesUtil.writeFileToClient(tempFile, response, fileName);
        FilesUtil.delete(tempFile);
    }

    public static void deleteFile(String dir, String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return;
        }
        String sendTime = DateUtil.getFullTime(DateUtil.getSysDate());
        String accessToken = DigestUtils.md5Hex((DigestUtils.md5Hex(sendTime + "&" + TOKEN_KEY + "@" + fileName)));
        String result = "";
        try {
            String hadoopSocket = getHadoopSocket();
            result = HttpRequest
                    .post("http://" + hadoopSocket + "/services/file/delete?filename=" + dir + File.separator + fileName)
                    .body("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                            "<req:Request xmlns:ns2=\"http://www.cn.dhl.com\">" +
                            "<RequestHeader>" +
                            "<SendTime>" + sendTime + "</SendTime> " +
                            "<SerialNo>818865224337</SerialNo> " +
                            "<ChannelName>gia</ChannelName>" +
                            "<AccessToken>" + accessToken + "</AccessToken>" +
                            "</RequestHeader>" +
                            "<FileName>" + fileName + "</FileName>" +
                            "<Type>" + FILE_DIR + "/" + dir + "</Type> " +
                            "</req:Request>"
                    )
                    .header("Content-Type", "application/xml")
                    .header("cache-control", "no-cache")
                    .header("Postman-Token", TOKEN_KEY)
                    .execute().body();

            if (!result.equalsIgnoreCase("{result:\"OK\"}")) {
                throw new ConnectionException("hadoop删除文件失败," + result);
            }
        } catch (Exception e) {
            throw new ConnectionException("hadoop连接失败");
        }
    }


    private static String getHadoopSocket() {
        RedisService redisService = SpringContextUtil.getBean(RedisService.class);
        String hadoopSocket = redisService.getSysConfigValue("hadoopSocket");
        if (StringUtil.isEmpty(hadoopSocket)) {
            throw new ConnectionException("hadoop地址没有配置");
        }
        return hadoopSocket;
    }

    private static byte[] getFileFromHadoop(String dir, String fileName) {

        String hadoopSocket = getHadoopSocket();
        try {
            HttpResponse result = HttpRequest
                    .get("http://" + hadoopSocket + "/services/file/get/caceinvoice?filename=" + dir + File.separator + fileName)
                    .header("Content-Type", "application/octet-stream")
                    .execute().sync();
            byte[] fileBytes = result.bodyBytes();
            return fileBytes;
        } catch (Exception e) {
            throw new ConnectionException("hadoop连接失败");
        }
    }


}






