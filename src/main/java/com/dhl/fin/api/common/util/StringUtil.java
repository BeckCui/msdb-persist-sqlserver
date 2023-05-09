package com.dhl.fin.api.common.util;

import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by CuiJianbo on 2020.02.18.
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    public static String join(String[] strs, String separator) {
        return strs.length > 0 ? Arrays.stream(strs).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining(separator)) : null;
    }

    public static String join(Object... strs) {
        return strs.length > 0 ? Arrays.stream(strs).filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining()) : null;
    }

    /**
     * 下划线转驼峰
     */
    public static String toCamelCase(String str) {
        return StrUtil.isNotEmpty(str) ? StrUtil.toCamelCase(str) : "";
    }


    /**
     * 驼峰转下划线
     */
    public static String toUnderlineCase(String str) {
        if (StrUtil.isNotEmpty(str)) {
            return StrUtil.toUnderlineCase(lowerFirst(str));
        } else {
            return "";
        }
    }

    /**
     * @param str
     * @return
     */
    public static String lowerFirst(String str) {
        return StrUtil.isNotEmpty(str) ? StrUtil.lowerFirst(str) : "";
    }

    public static String upperFirst(String str) {

        return StrUtil.isNotEmpty(str) ? StrUtil.upperFirst(str) : "";
    }

    public static String upperFirstAndAddPre(String str, String preString) {
        return StrUtil.isNotEmpty(str) ? StrUtil.upperFirstAndAddPre(str, preString) : "";
    }

    public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0) {
                        k += 256;
                    }
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

}
