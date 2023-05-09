package com.dhl.fin.api.common.enums;

import lombok.Getter;

/**
 * @author CuiJianbo
 * @date 2020.02.22
 */
@Getter
public enum MsgTypeEnum {

    SUCCESS("success", "成功"),

    ERROR("error", "错误"),

    INFO("info", "信息");


    private String code;
    private String name;

    MsgTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
