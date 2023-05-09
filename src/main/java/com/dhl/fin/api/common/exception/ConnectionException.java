package com.dhl.fin.api.common.exception;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;

/**
 * @author becui
 * @date 8/6/2020
 */
public class ConnectionException extends BusinessException {
    private Boolean sendMail = false;

    public ConnectionException(String message) {
        super("无", ActionEnum.CONNECTION, LogStatus.FAILED, message);
    }

    public ConnectionException(String message, Boolean sendMail) {
        super("无", ActionEnum.CONNECTION, LogStatus.FAILED, message);
        this.sendMail = sendMail;
    }
}


