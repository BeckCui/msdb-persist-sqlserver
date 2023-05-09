package com.dhl.fin.api.common.enums;

import lombok.Getter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

@Getter
public enum ExcelCellAlignment {

    CENTER(HorizontalAlignment.CENTER),
    LEFT(HorizontalAlignment.LEFT),
    RIGHT(HorizontalAlignment.RIGHT);

    private HorizontalAlignment name;

    ExcelCellAlignment(HorizontalAlignment name) {
        this.name = name;
    }

}
