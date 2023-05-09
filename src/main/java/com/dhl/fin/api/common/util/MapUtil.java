package com.dhl.fin.api.common.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.MapUtils;

import java.util.*;

/**
 * Created by CuiJianbo on 2020.02.21.
 */
public class MapUtil {

    public static <K, V> MapBuilder builder() {
        return new MapBuilder<K, V>();
    }

    private static <T> T getValue(Class<T> type, Object value) {
        if (value.getClass() == type) {
            return (T) value;
        } else if (!Objects.isNull(value)) {
            String strValue = Objects.toString(value);
            if (type == String.class) {
                return (T) strValue;
            } else if (type == Date.class) {
                return (T) DateUtil.getDate(strValue);
            }
        }
        return null;
    }

    public static boolean isEmpty(Map map) {
        return MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map map) {
        return MapUtils.isNotEmpty(map);
    }

    public static Map putAll(Map map, Object[] appendAarray) {
        return MapUtils.putAll(map, appendAarray);
    }

    public static boolean hasKey(Map map, Object key) {
        return ObjectUtil.notNull(MapUtils.getObject(map, key));
    }

    public static String getString(Map map, Object key, String... defaultValue) {
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue[0];
            }
        } else {
            if (v instanceof Object[]) {
                return ((Object[]) v)[0].toString();
            } else {
                return v.toString().trim().replace("\u00A0", "");
            }
        }
    }

    public static String[] getStringArray(Map map, Object key, String... defaultValue) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            if (v instanceof Object[]) {
                return Arrays.stream(((Object[]) v))
                        .map(Object::toString)
                        .toArray(String[]::new);
            } else {
                return null;
            }
        }
    }

    public static Integer getInteger(Map map, Object key, Integer... defaultValue) {
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue[0];
            }
        } else {
            if (v instanceof Object[]) {
                return Integer.valueOf(((Object[]) v)[0].toString());
            } else if (StringUtil.isEmpty(v.toString().trim())) {
                return null;
            } else {
                return Integer.valueOf(v.toString().trim().replace("\u00A0", ""));
            }
        }
    }

    public static Integer[] getIntegerArray(Map map, Object key, Integer... defaultValue) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            if (v instanceof Integer[]) {
                return Arrays.stream(((Object[]) v))
                        .map(Object::toString)
                        .map(Integer::valueOf)
                        .toArray(Integer[]::new);
            } else {
                return null;
            }
        }
    }


    public static Double getDouble(Map map, Object key, Double... defaultValue) {
        Double value = null;
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isNotEmpty(defaultValue)) {
                value = defaultValue[0];
            }
        } else {
            if (v instanceof Object[]) {
                value = Double.valueOf(((Object[]) v)[0].toString());
            } else if (StringUtil.isEmpty(v.toString().trim())) {
                return null;
            } else {
                value = Double.valueOf(v.toString().trim().replace("\u00A0", ""));
            }
        }

        return ObjectUtil.isNull(value) ? null : NumberUtil.round(value, 2).doubleValue();
    }

    public static Double[] getDoubleArray(Map map, Object key, Double... defaultValue) {
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            if (v instanceof Double[]) {
                return Arrays.stream(((Object[]) v))
                        .map(Object::toString)
                        .map(Integer::valueOf)
                        .toArray(Double[]::new);
            } else {
                return null;
            }
        }
    }


    public static Long getLong(Map map, Object key, Long... defaultValue) {
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue[0];
            }
        } else {
            if (v instanceof Object[]) {
                return Long.valueOf(((Object[]) v)[0].toString());
            } else if (StringUtil.isEmpty(v.toString().trim())) {
                return null;
            } else {
                return Long.valueOf(v.toString().trim().replace("\u00A0", ""));
            }
        }
    }

    public static Long[] getLongArray(Map map, Object key, Long... defaultValue) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue;
            }
        } else {
            if (v instanceof Object[]) {
                return Arrays.stream(((Object[]) v))
                        .map(Object::toString)
                        .filter(StringUtil::isNotEmpty)
                        .map(Long::valueOf)
                        .toArray(Long[]::new);
            } else {
                return null;
            }
        }
    }

    public static <T> T getObjectParam(Map map, String key, Class<T> classType) {
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            return null;
        } else {
            return (T) v;
        }
    }

    public static Object getObject(Map map, String key) {
        return MapUtils.getObject(map, key);
    }

    public static <T> T getObject(Map map, String key, Class<T> classType) {
        Object o = MapUtils.getObject(map, key);
        if (ObjectUtil.notNull(o)) {
            return (T) o;
        } else {
            return null;
        }
    }


    public static Date getDate(Map map, Object key, Date... defaultValue) {
        Object v = MapUtils.getObject(map, key);

        if (!(v instanceof Date)) {
            return null;
        }

        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return null;
            } else {
                return defaultValue[0];
            }
        } else {
            return (Date) v;
        }
    }

    public static Boolean getBoolean(Map map, Object key, Boolean... defaultValue) {
        Object v = MapUtils.getObject(map, key);
        if (ObjectUtil.isNull(v)) {
            if (ArrayUtil.isEmpty(defaultValue)) {
                return false;
            } else {
                return defaultValue[0];
            }
        } else {
            if (v instanceof Object[]) {
                return Boolean.valueOf(((Object[]) v)[0].toString());
            } else {
                return Boolean.valueOf(v.toString().trim().replace("\u00A0", ""));
            }
        }
    }

    public static JSONObject getJsonObject(Map map, Object key) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            return null;
        } else {
            if (v instanceof JSONObject) {
                return (JSONObject) v;
            } else {
                return null;
            }
        }

    }

    public static JSONArray getJsonArray(Map map, Object key) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            return null;
        } else {
            if (v instanceof JSONArray) {
                return (JSONArray) v;
            } else {
                return null;
            }
        }
    }

    public static Map getMap(Map map, Object key) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            return null;
        } else {
            if (v instanceof Map) {
                return (Map) v;
            } else if (v instanceof Map) {
                return null;
            } else {
                return null;
            }
        }

    }

    public static List getList(Map map, Object key) {
        Object v = MapUtils.getObject(map, key);

        if (ObjectUtil.isNull(v)) {
            return null;
        } else {
            if (v instanceof List) {
                return (List) v;
            } else {
                return null;
            }
        }

    }


    public static Map beanToMap(Object obj) {
        return BeanUtil.beanToMap(obj);
    }

    public static <T> T mapToBean(Map obj, Class<T> type) {
        return BeanUtil.mapToBean(obj, type, true);
    }

}
