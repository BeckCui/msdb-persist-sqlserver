package com.dhl.fin.api.common.enums;

import com.dhl.fin.api.common.util.StringUtil;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author becui
 * @date 4/22/2020
 */
@Getter
public enum CacheKeyEnum {

    DICTIONARIES_PER_APP("dictionaries_app", "每个项目对应的数据字典"),

    SYSTEM_CONFIG("systemConfig", "系统配置"),

    ACTIVE_USER("appActiveUser", "每个应用的活跃用户数量统计"),

    ALL_PROJECTS("allProjects", "缓存每个应用的数据"),

    LOGIN_USERS("loginUsers", "登录用户"),

    UAM("uam", "user review 的实时状态"),

    FORBIDDEN_APP("forbiddenApp", "被禁用的应用有哪些"),

    SUPER_MANAGER("superManager", "超级管理员");

    private String code;
    private String remark;

    CacheKeyEnum(String code, String remark) {
        this.code = code;
        this.remark = remark;
    }

    public static CacheKeyEnum getByCode(String code) {
        if (StringUtil.isEmpty(code)) {
            return null;
        }
        return Arrays.stream(CacheKeyEnum.values()).filter(p -> p.getCode().equals(code)).findFirst().orElse(null);
    }

}
