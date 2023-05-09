package com.dhl.fin.api.common.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * Created by CuiJianbo on 2020.02.19.
 */
public class ArrayUtil {


    public static String[] arrayString() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public static Integer[] arrayInteger() {
        return ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
    }

    public static Long[] arrayLong() {
        return ArrayUtils.EMPTY_LONG_OBJECT_ARRAY;
    }

    public static Double[] arrayDouble() {
        return ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY;
    }

    public static boolean isEmpty(Object[] data) {
        return ArrayUtils.isEmpty(data) || data.length == 0;
    }

    public static boolean isNotEmpty(Object[] data) {
        return !isEmpty(data);
    }

    public static <T> boolean contains(T[] data, T value) {
        if (ObjectUtil.isNull(value)) {
            return false;
        }
        return ArrayUtils.contains(data, value);
    }

    public static <T> T[] insert(T[] data, int index, T value) {
        if (ObjectUtil.isNull(value)) {
            return data;
        }
        return ArrayUtils.insert(index, data, value);
    }

    public static <T> void push(T[] data, T item) {
        if (ObjectUtil.notNull(item) && ObjectUtil.notNull(data)) {
            ArrayUtils.add(data, item);
        }
    }

    public static <T> T pop(T[] data) {
        T item = null;
        if (ObjectUtil.notNull(data) && data.length > 1) {
            int lastIndex = data.length - 1;
            item = data[lastIndex];
            ArrayUtils.remove(data, lastIndex);
        }

        return item;
    }

    public static <T> T[] remove(T[] data, int index) {
        if (data.length <= index || index < 0) {
            return data;
        }
        return ArrayUtils.remove(data, index);
    }

    public static <T> T[] addAll(T[] array1, T[] array2) {
        return ArrayUtils.addAll(array1, array2);
    }

    public static String join(Object[] objArray, String operator) {
        return !ObjectUtil.isNull(objArray) && objArray.length > 0 ? Arrays.stream(objArray).map(Object::toString).collect(joining(operator)) : "";
    }

    public static <T> List<T> arrayToList(T[] datas) {
        return Arrays.asList(datas);
    }

}
