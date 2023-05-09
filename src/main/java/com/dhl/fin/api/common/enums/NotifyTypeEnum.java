package com.dhl.fin.api.common.enums;

import lombok.Getter;

/**
 * @author CuiJianbo
 * @date 2020.02.22
 */
@Getter
public enum NotifyTypeEnum {

    /**
     * 前端页面右上角会弹出message信息
     */
    NOTIFY("notify", "通知"),


    /**
     * 前端页面中间会弹出message信息，3秒后自动消失
     */
    MESSAGE("message", "信息"),

    /**
     * 前端页面中间会弹出message提示框，用户主动点击确定才消失提示框
     */
    ALERT("alert", "警示");


    private String code;
    private String name;

    NotifyTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
