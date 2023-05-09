package com.dhl.fin.api.common.enums;


import lombok.Getter;

@Getter
public enum EnvType {

    DEV("dev"),
    PROD("prod"),
    UAT("uat"),
    TEST("test");

    private String type;


    EnvType(String type) {
        this.type = type;
    }


}
