package com.dhl.fin.api.common.service;

import com.dhl.fin.api.common.dto.LoginUserPermissionDto;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 5/19/2020
 */
@Service
public class RedisService {

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${custom.projectCode}")
    private String projectCode;


    private Map getRoot() {
        String platformRootName = projectCode.split("_")[0];
        Object root = redisTemplate.opsForValue().get(platformRootName);
        if (ObjectUtil.isNull(root)) {
            redisTemplate.opsForValue().set(platformRootName, MapUtil.builder().build());
        }
        Object rootData = redisTemplate.opsForValue().get(platformRootName);
        return JsonUtil.parseToMap(rootData.toString());
    }

    public void put(CacheKeyEnum key, Object value) {
        if (ObjectUtil.notNull(key)) {
            String platformRootName = projectCode.split("_")[0];
            Map rootData = getRoot();
            rootData.put(key.getCode(), value);
            value = JsonUtil.objectToString(rootData);
            redisTemplate.opsForValue().set(platformRootName, value);
        }
    }

    public Object delete(CacheKeyEnum key) {
        if (StringUtil.isNotEmpty(key.getCode())) {
            String platformRootName = projectCode.split("_")[0];
            Map rootData = getRoot();
            Object data = rootData.remove(key.getCode());
            String value = JsonUtil.objectToString(rootData);
            redisTemplate.opsForValue().set(platformRootName, value);
            return data;
        } else {
            return null;
        }
    }


    public Object get(CacheKeyEnum key) {
        if (ObjectUtil.notNull(key)) {
            Map rootData = getRoot();
            return rootData.get(key.getCode());
        } else {
            return null;
        }
    }

    public LoginUserPermissionDto getUserPermission(String uuid) {
        Map user = getMap(CacheKeyEnum.LOGIN_USERS);

        if (ObjectUtil.notNull(user)) {
            return JsonUtil.parseToJavaBean(MapUtil.getString(user, uuid), LoginUserPermissionDto.class);
        }

        return null;
    }

    /**
     * 通过key  获取value
     *
     * @param parentCode
     * @param option
     * @return
     */
    public String getDictionaryValue(String parentCode, String option) {
        Map disc = getDictionary(parentCode);
        if (ObjectUtil.notNull(disc)) {
            return MapUtil.getString(disc, option);
        }
        return null;
    }

    /**
     * 通过value 获取key
     *
     * @param parentCode
     * @param value
     * @return
     */
    public String getDictionaryKey(String parentCode, String value) {
        Map<String, String> disc = getDictionary(parentCode);
        for (Map.Entry<String, String> item : disc.entrySet()) {
            if (item.getValue().equals(value)) {
                return item.getKey();
            }
        }
        return null;
    }

    public Map getDictionary(String parentCode) {
        Map dicMap = getMap(CacheKeyEnum.DICTIONARIES_PER_APP);
        if (ObjectUtil.notNull(dicMap)) {
            String[] temp = projectCode.split("_");
            String pcode = temp.length > 1 ? temp[1] : temp[0];
            Map dictionaries = MapUtil.getMap(dicMap, pcode);
            if (ObjectUtil.notNull(dictionaries)) {
                return MapUtil.getMap(dictionaries, parentCode);
            }
        }
        return null;
    }

    public String getSysConfigValue(String key) {
        Map sysMap = getMap(CacheKeyEnum.SYSTEM_CONFIG);
        if (ObjectUtil.notNull(sysMap)) {
            return MapUtil.getString(sysMap, key);
        }
        return null;
    }

    public Map getMap(CacheKeyEnum key) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof String) {
                return JsonUtil.parseToMap(o.toString());
            } else if (o instanceof Map) {
                return (Map) o;
            }
        }
        return null;

    }

    public List<Map> getList(CacheKeyEnum key) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof String) {
                return JsonUtil.parseToList(o.toString());
            } else if (o instanceof List) {
                return (List) o;
            }
        }
        return null;
    }

    public <T> List<T> getList(CacheKeyEnum key, Class<T> type) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof String) {
                return JsonUtil.parseToList(o.toString(), type);
            } else if (o instanceof List) {
                return (List<T>) o;
            }
        }
        return null;
    }

    public String getString(CacheKeyEnum key) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof String) {
                return o.toString();
            }
        }
        return null;
    }

    public Integer getInteger(CacheKeyEnum key) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof Integer) {
                return (Integer) o;
            }
        }
        return null;
    }

    public Long getLong(CacheKeyEnum key) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof Long) {
                return (Long) o;
            }
        }
        return null;
    }

    public Double getDouble(CacheKeyEnum key) {
        Object o = get(key);

        if (ObjectUtil.notNull(o)) {
            if (o instanceof Double) {
                return (Double) o;
            }
        }
        return null;
    }

    public Map getProject(String appCode) {

        if (appCode.equalsIgnoreCase("fintp")) {
            return MapUtil.builder()
                    .add("code", "fintp")
                    .add("name", "FINTP")
                    .add("enName", "FINTP")
                    .build();
        }
        List<Map> apps = getList(CacheKeyEnum.ALL_PROJECTS);
        if (CollectorUtil.isNoTEmpty(apps)) {
            return apps.stream()
                    .filter(p -> appCode.contains(MapUtil.getString(p, "code")))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public List<Map> getAllSuperMangers() {
        return getList(CacheKeyEnum.SUPER_MANAGER);
    }
}
