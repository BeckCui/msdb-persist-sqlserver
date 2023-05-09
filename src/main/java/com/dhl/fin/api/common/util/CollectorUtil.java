package com.dhl.fin.api.common.util;

import cn.hutool.core.collection.CollUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CuiJianbo on 2020.02.27.
 */
public class CollectorUtil {


    public static boolean isEmpty(List list) {
        return CollectionUtils.isEmpty(list);
    }

    public static boolean isNoTEmpty(List list) {
        return CollectionUtils.isNotEmpty(list);
    }

    public static List distinct(Collection collection) {
        return CollUtil.distinct(collection);
    }

    public static <T> List<T> toList(T... object) {
        return Arrays.asList(object);
    }

    public static String join(List list) {
        return join(list, "");
    }

    public static String join(List list, String operator) {
        if (isNoTEmpty(list)) {
            return ArrayUtil.join(list.stream().map(Object::toString).toArray(Object[]::new), operator);
        } else {
            return null;
        }
    }

    public static Map<String, Object> ListMapToMap(List<Map<String, Object>> data, String key) {
        if (isNoTEmpty(data)) {
            MapBuilder mapData = MapUtil.builder();
            for (Map<String, Object> item : data) {
                mapData.add(MapUtil.getString(item, key), item);
            }
            return mapData.build();
        }
        return null;
    }


}






