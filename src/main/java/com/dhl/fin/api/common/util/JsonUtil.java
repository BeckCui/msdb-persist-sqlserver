package com.dhl.fin.api.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 5/21/2020
 */
public class JsonUtil {

    public static String objectToString(Object o) {
        return ObjectUtil.isNull(o) ? null : JSON.toJSONString(o);
    }


    public static List<Map> parseToList(String obj) {
        return StringUtil.isEmpty(obj) ? null : JSON.parseArray(obj, Map.class);
    }

    public static <T> List<T> parseToList(String obj, Class<T> type) {
        return StringUtil.isEmpty(obj) ? null : JSON.parseArray(obj, type);
    }

    public static Map parseToMap(String obj) {
        return StringUtil.isEmpty(obj) ? null : JSON.parseObject(obj, Map.class);
    }

    public static <T> T parseToJavaBean(String obj, Class<T> type) {
        return StringUtil.isEmpty(obj) ? null : JSON.parseObject(obj, type);
    }

    public static String[] jsonArrayToArray(JSONArray jsonArray) {
        String[] stringArray = null;
        if (jsonArray != null) {
            int length = jsonArray.size();
            stringArray = new String[length];
            for (int i = 0; i < length; i++) {
                stringArray[i] = jsonArray.getString(i);
            }
        }
        return stringArray;
    }


}




