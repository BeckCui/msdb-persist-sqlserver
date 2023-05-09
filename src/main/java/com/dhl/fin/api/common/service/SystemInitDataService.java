package com.dhl.fin.api.common.service;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import com.dhl.fin.api.common.annotation.DictionaryEnum;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 4/6/2020
 */
@Slf4j
@Component
public class SystemInitDataService implements ApplicationRunner {


    @Autowired
    private RedisService redisService;

    @Override
    public void run(ApplicationArguments args) {

        addDictionaryToRedis();

        log.info("********************启动完成********************");

    }


    /**
     * 缓存枚举类对象
     */
    private void addDictionaryToRedis() {
        try {
            Map perAppMap = redisService.getMap(CacheKeyEnum.DICTIONARIES_PER_APP);
            if (ObjectUtil.isNull(perAppMap)) {
                perAppMap = new HashMap();
            }
            String projectCode = SpringContextUtil.getPropertiesValue("custom.projectCode");
            if (projectCode.contains("_")) {
                projectCode = projectCode.split("_")[1];
            }
            List<Class> classes = ObjectUtil.getClasses("com.dhl.fin.api.enums");
            List<Class> commonClasses = ObjectUtil.getClasses("com.dhl.fin.api.common.enums");
            if (!projectCode.contains("_")) {
                classes.addAll(commonClasses);
            }

            Map projectCodeMap = MapUtil.getMap(perAppMap, projectCode);
            if (ObjectUtil.isNull(projectCodeMap)) {
                projectCodeMap = new HashMap();
            }

            for (Class enumClass : classes) {
                Object o = enumClass.getDeclaredAnnotation(DictionaryEnum.class);
                if (ObjectUtil.notNull(o)) {
                    DictionaryEnum dictionaryEnum = (DictionaryEnum) o;
                    Map childDictionary = new HashMap();
                    String parentCode = dictionaryEnum.code();
                    LinkedHashMap<String, Enum> children = EnumUtil.getEnumMap(enumClass);
                    for (Map.Entry<String, Enum> child : children.entrySet()) {
                        String code = ReflectUtil.getFieldValue(child.getValue(), "code").toString();
                        String name = ReflectUtil.getFieldValue(child.getValue(), "name").toString();
                        childDictionary.put(code, name);
                    }
                    projectCodeMap.put(parentCode, childDictionary);
                }
            }
            perAppMap.put(projectCode, projectCodeMap);
            redisService.put(CacheKeyEnum.DICTIONARIES_PER_APP, perAppMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }


}

