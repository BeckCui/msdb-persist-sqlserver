package com.dhl.fin.api.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by CuiJianbo on 2020.02.20.
 */

@Setter
@Getter
public class QueryDto {


    /**
     * 查询起始下标
     */
    private int startIndex;

    /**
     * 每页数据大小
     */
    private int length;

    /**
     * where 条件
     */
    private List<String> whereCondition = new LinkedList<>();

    /**
     * order 条件
     */
    private List<String> orderCondition = new LinkedList<>();

    private String orderFields;

    /**
     * 查询的字段
     */
    private String fields;

    /**
     * 关联的表有哪些
     */
    private List<String> joinDomain = new LinkedList<>();

    /**
     * 关联的表有哪些(Map: 存table表和表别名)
     */
    private List<Map> joinMap = new LinkedList<>();

    /**
     * 对remove做筛选
     */
    private Boolean remove;

    /**
     * 对remove做筛选
     */
    private Boolean selectOne = false;

    public static QueryBuilder builder() {
        return new QueryBuilder();
    }
}
