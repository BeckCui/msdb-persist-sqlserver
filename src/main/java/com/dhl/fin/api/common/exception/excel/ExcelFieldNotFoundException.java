package com.dhl.fin.api.common.exception.excel;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.exception.BusinessException;

/**
 * @author becui
 * @date 5/27/2020
 */
public class ExcelFieldNotFoundException extends BusinessException {

    public ExcelFieldNotFoundException() {
        super("无", ActionEnum.IMPORT, LogStatus.FAILED, "excel的列名不符合模板要求");
    }
}
