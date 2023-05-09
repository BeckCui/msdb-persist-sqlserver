package com.dhl.fin.api.common.util;

import cn.hutool.core.util.ZipUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 8/7/2020
 */
public class CompressUtil {

    public static String compressStr(String content) {
        if (StringUtil.isNotEmpty(content)) {
            byte[] byteStr = ZipUtil.gzip(content, "UTF-8");
            StringBuffer sb = new StringBuffer();
            for (byte s : byteStr) {
                sb.append(s + "|");
            }

            return sb.toString();
        } else {
            return null;
        }
    }

    public static String unCompressStr(String content) {
        if (StringUtil.isNotEmpty(content)) {
            List<Byte> conByte = Arrays.stream(content.split("\\|")).map(p -> Byte.valueOf(p))
                    .collect(Collectors.toList());
            byte[] byteContent = new byte[conByte.size()];
            for (int i = 0; i < conByte.size(); i++) {
                byteContent[i] = conByte.get(i);
            }
            return ZipUtil.unGzip(byteContent, "UTF-8");
        } else {
            return null;
        }
    }


}





