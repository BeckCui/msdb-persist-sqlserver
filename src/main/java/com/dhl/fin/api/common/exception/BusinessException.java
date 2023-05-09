package com.dhl.fin.api.common.exception;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by CuiJianbo on 2020.03.03.
 */

@Getter
@Setter
public class BusinessException extends RuntimeException {

    private String tableName;

    private ActionEnum actionEnum;

    private LogStatus logStatus;

    private String message;

    public BusinessException() {

    }

    public BusinessException(String message) {
        super(message);
        this.tableName = "";
        this.actionEnum = ActionEnum.OTHER;
        this.logStatus = LogStatus.FAILED;
        this.message = message;
    }

    public BusinessException(String tableName, ActionEnum actionEnum, LogStatus logStatus, String message) {
        super(message);
        this.tableName = tableName;
        this.actionEnum = actionEnum;
        this.logStatus = logStatus;
        this.message = message;
    }

    public String getMessage() {
        return super.getMessage();
    }


}



