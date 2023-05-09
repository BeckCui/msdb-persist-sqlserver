package com.dhl.fin.api.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CuiJianbo on 2020.02.21.
 */
public class MapBuilder<K, V> {
    private Map mapdata = new HashMap();

    public MapBuilder add(K key, V value, V... defaultValue) {
        if (ObjectUtil.notNull(value)) {
            this.mapdata.put(key, value);
        } else {
            if (ArrayUtil.isNotEmpty(defaultValue)) {
                this.mapdata.put(key, defaultValue[0]);
            }
        }
        return this;
    }

    public MapBuilder addAll(Map data) {
        mapdata.putAll(data);
        return this;
    }

    public MapBuilder addWhen(K key, V value, boolean isAdd) {
        if (ObjectUtil.notNull(value) && isAdd) {
            this.mapdata.put(key, value);
        }
        return this;
    }

    public Map build() {
        return this.mapdata;
    }
}
