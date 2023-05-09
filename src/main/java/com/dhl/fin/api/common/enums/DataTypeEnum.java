package com.dhl.fin.api.common.enums;

import com.dhl.fin.api.common.util.StringUtil;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by CuiJianbo on 2020.03.09.
 */
@Getter
public enum DataTypeEnum {
    INT("i", "整型"),

    LONG("l", "长整型"),

    STRING("s", "字符串"),

    ARRAY_STRING("[s]", "字符串数组"),

    ARRAY_INT("[i]", "整型数组");


    private String code;
    private String name;

    DataTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }



    public static DataTypeEnum getByCode(String code) {
        if (StringUtil.isEmpty(code)) {
            return null;
        }
        return Arrays.stream(DataTypeEnum.values()).filter(p -> p.getCode().equals(code)).findFirst().orElse(null);
    }
}
