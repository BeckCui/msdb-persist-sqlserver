package com.dhl.fin.api.common.util.excel;

import com.dhl.fin.api.common.enums.ExcelCellAlignment;
import com.dhl.fin.api.common.enums.ExcelDataType;
import lombok.Builder;
import lombok.Data;

/**
 * @author becui
 * @date 4/17/2020
 */

@Data
@Builder
public class ExcelTitleBean {

    private String key;
    private String code;
    private String name;
    private String dicCode;
    private ExcelDataType dataType;
    private ExcelCellAlignment alignment;
    private int width;
    private int sort;

    @Builder.Default
    private boolean available = true;


}
