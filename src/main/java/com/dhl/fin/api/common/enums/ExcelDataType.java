package com.dhl.fin.api.common.enums;

/**
 * @author becui
 * @date 5/19/2020
 */
public enum ExcelDataType {

    INTEGER("integer"),
    LONG("long"),
    DOUBLE("double"),
    TEXT("text"),
    DATE("date"),
    TIME("time");

    private String type;

    ExcelDataType(String type) {
        this.type = type;
    }

}
