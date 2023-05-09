package com.dhl.fin.api.common.enums;

import lombok.Getter;

/**
 * Created by CuiJianbo on 2020.02.22.
 */
@Getter
public enum OperatorEnum {
    /**
     * equal
     */
    EQ("eq", "="),
    /**
     * not equal
     */
    NOTEQ("ne", "!="),
    /**
     * 模糊查询
     */
    LIKE("like", "like"),
    LIKES("likes", "likes"),
    /**
     * between
     */
    BT("between", "between"),
    /**
     * less than.
     */
    LT("lt", "<"),
    /**
     * greater than.
     */
    GT("gt", ">"),
    /**
     * less equals.
     */
    LE("le", "<="),
    /**
     * greater equals.
     */
    GE("ge", ">="),
    /**
     * in.
     */
    IN("in", "in"),
    /**
     * not in.
     */
    NOTIN("notIn", "not in");

    private String code;

    private String operator;

    OperatorEnum(String code, String operator) {
        this.code = code;
        this.operator = operator;
    }


}


