package com.dhl.fin.api.common.util.excel;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by CuiJianbo on 2020.03.22.
 */
@Data
public class ExcelSheet {


    /**
     * sheet 名字
     */
    private String sheetName;

    /**
     * 表格head的名字
     */
    private String headName;

    /**
     * 数据
     */
    private List rowList = new LinkedList<>();


    /**
     * title 排序
     * map {
     * sort,       排序
     * key,  title key
     * name,  title 中文名
     * dicCode     如果是数据字典值，就传入数据字典code
     * }
     */
    private List<ExcelTitleBean> titles = new LinkedList<>();

    /**
     * 分页加载数据到内存里的数据量大小
     */
    private int pageSize = 100000;

    /**
     * 用于分页读取数据，导入内存
     * 指定数据库Service
     */
    private CommonDBService commonDBService;

    /**
     * 用于分页读取数据，导入内存
     * 自定义的查询sql
     */
    private String sql;


    public static ExcelSheetBuilder builder() {
        return new ExcelSheetBuilder();
    }

}
