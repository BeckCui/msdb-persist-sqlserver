package com.dhl.fin.api.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.io.*;
import java.util.Random;

/**
 * @author becui
 * @date 8/4/2020
 */
public class SecUtil {

    private static final String privateKey = "GHVYnawfbrAe4nR1";


    /**
     * 加密
     *
     * @param content
     * @return
     */
    public static String encrypt(String content) {
        AES aes = SecureUtil.aes(privateKey.getBytes());
        return aes.encryptHex(content);
    }

    /**
     * 解密
     *
     * @param content
     * @return
     */
    public static String decrypt(String content) {
        AES aes = SecureUtil.aes(privateKey.getBytes());
        return aes.decryptStr(content);
    }


    /**
     * md5加密
     *
     * @param bytes
     * @return
     */
    public static String md5Encrypt(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return SecureUtil.md5().digestHex(bytes);
    }

    /**
     * 随机密码
     *
     * @param length
     * @return
     */
    public static String alphaNumeric(int length, boolean bigChart, boolean smallChart, boolean number) {


        char[] buf;

        // 添加一些特殊字符
        buf = new char[length];

        // 固定设置10个数字类型的
        String flag = "";
        int count = 0;

        if (number) {
            count = numberAlg(buf, count, false);
            flag = "number";
        }


        if (smallChart && count > 0) {
            count = smallChartAlg(buf, count, false);
            flag = "smallChart";
        }

        if (bigChart && count > 0) {
            count = bigChartAlg(buf, count, false);
            flag = "bigChart";
        }

        if (count > 0) {
            if (flag.equalsIgnoreCase("number")) {
                numberAlg(buf, count, true);
            } else if (flag.equalsIgnoreCase("smallChart")) {
                smallChartAlg(buf, count, true);
            } else if (flag.equalsIgnoreCase("bigChart")) {
                bigChartAlg(buf, count, true);
            }
        }


        return new String(buf);

    }

    private static int numberAlg(char[] buf, int count, boolean isFull) {
        Random random = new Random();
        char[] numberChart = "23456789".toCharArray();
        int i = 0;
        count = isFull ? count : random.nextInt(buf.length / 2);
        if (count == 0) {
            count = 1;
        }
        while (i < count) {
            int index = random.nextInt(buf.length);

            if (buf[index] == '\u0000') {
                buf[index] = numberChart[random.nextInt(numberChart.length)];
                i++;
            }
        }
        return buf.length - count;
    }

    private static int smallChartAlg(char[] buf, int count, boolean isFull) {

        Random random = new Random();
        StringBuilder lowerChart = new StringBuilder();
        for (char ch = 'a'; ch <= 'z'; ++ch)
            lowerChart.append(ch);
        char[] symbolsLower = lowerChart.toString().toCharArray();

        int length = buf.length;
        int remainCount = count;
        int i = 0;
        count = isFull ? count : random.nextInt(count / 2);
        if (count == 0) {
            count = 1;
        }
        while (i < count) {
            int index = random.nextInt(length);
            if (buf[index] == '\u0000') {
                buf[index] = symbolsLower[random.nextInt(symbolsLower.length)];
                i++;
            }
        }
        return remainCount - count;
    }

    private static int bigChartAlg(char[] buf, int count, boolean isFull) {

        Random random = new Random();
        StringBuilder upperChart = new StringBuilder();
        for (char ch = 'A'; ch <= 'Z'; ++ch)
            upperChart.append(ch);

        char[] symbolsUpper = upperChart.toString().toCharArray();

        int remainCount = count;
        count = isFull ? count : random.nextInt(count / 2);
        if (count == 0) {
            count = 1;
        }
        int i = 0;
        while (i < count) {
            int index = random.nextInt(buf.length);
            if (buf[index] == '\u0000') {
                buf[index] = symbolsUpper[random.nextInt(symbolsUpper.length)];
                i++;
            }
        }

        return remainCount - count;
    }


    private static PooledPBEStringEncryptor getEncryptor(String key, String algorithm, String poolSize, String iterations) {
        algorithm = StringUtil.isEmpty(algorithm) ? "PBEWITHHMACSHA512ANDAES_256" : algorithm;
        poolSize = StringUtil.isEmpty(poolSize) ? "1" : poolSize;
        iterations = StringUtil.isEmpty(iterations) ? "1000" : iterations;

        String ENCRYPTOR_PASSWORD = StringUtil.isEmpty(key) ? SpringContextUtil.getPropertiesValue("jasypt.encryptor.password") : key;
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(ENCRYPTOR_PASSWORD);
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations(iterations);
        config.setPoolSize(poolSize);
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");

        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * Jasypt生成加密结果
     *
     * @param text 加密值
     */
    public static String pbeEncrypt(String text) {
        return getEncryptor(null, null, null, null).encrypt(text);
    }

    /**
     * Jasypt生成加密结果
     *
     * @param text 加密值
     */
    public static String pbeEncrypt(String text, String key, String algorithm, String poolSize, String iterations) {
        return getEncryptor(key, algorithm, poolSize, iterations).encrypt(text);
    }

    /**
     * Jasypt解密
     *
     * @param encryptedText 待解密的密文
     */
    public static String pbeDecrypt(String encryptedText) {
        return getEncryptor(null, null, null, null).decrypt(encryptedText);
    }

    /**
     * Jasypt解密
     *
     * @param encryptedText 待解密的密文
     */
    public static String pbeDecrypt(String encryptedText, String key, String algorithm, String poolSize, String iterations) {
        return getEncryptor(key, algorithm, poolSize, iterations).decrypt(encryptedText);
    }


    /**
     * 获取文件的MD5值
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String getFileMd5(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(in);
        String md5 = DigestUtils.md5Hex(IOUtils.toByteArray(bis));
        in.close();
        bis.close();
        return md5;
    }

}




