package com.dhl.fin.api.common.enums;


import lombok.Getter;

@Getter
public enum StoreLocationEnum {

    LOCAL("LOCAL"),

    FTP("FTP"),

    HADOOP("HADOOP");

    private String name;

    StoreLocationEnum(String type) {
        this.name = type;
    }

}
