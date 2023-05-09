package com.dhl.fin.api.common.util.excel;

import java.util.List;

/**
 * Created by CuiJianbo on 2020.03.22.
 */

public class ExcelBuilder {

    private ExcelUtil excelUtil = new ExcelUtil();

    public ExcelBuilder addSheet(ExcelSheet excelSheet) {
        excelUtil.getExcelSheets().add(excelSheet);
        return this;
    }

    public ExcelBuilder addSheetWhen(ExcelSheet excelSheet, Boolean isAdd) {

        if (isAdd) {
            excelUtil.getExcelSheets().add(excelSheet);
        }

        return this;
    }

    public ExcelBuilder addSheetAll(List<ExcelSheet> excelSheets) {
        excelUtil.getExcelSheets().addAll(excelSheets);
        return this;
    }

    public ExcelBuilder setFileName(String fileName) {
        excelUtil.setFileName(fileName);
        return this;
    }

    public ExcelBuilder setIsTailDate(boolean isTailDate) {
        excelUtil.setIsTrailDate(isTailDate);
        return this;
    }


    public ExcelUtil build() {
        return excelUtil;
    }

}
