package com.dhl.fin.api.common.util;

import com.dhl.fin.api.common.enums.CacheKeyEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author becui
 * @date 4/6/2020
 */
public class CacheUtil {

    private static Map<CacheKeyEnum, Object> data = new HashMap<>();


    public static void put(CacheKeyEnum key, Object value) {
        if (ObjectUtil.notNull(key) && ObjectUtil.notNull(value)) {
            data.put(key, value);
        }
    }

    public static Object get(CacheKeyEnum key) {
        return data.get(key);
    }

}
