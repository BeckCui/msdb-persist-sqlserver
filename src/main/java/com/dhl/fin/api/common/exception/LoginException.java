package com.dhl.fin.api.common.exception;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;

/**
 * Created by CuiJianbo on 2020.03.10.
 */
public class LoginException extends BusinessException {
    public LoginException(String message) {
        super("无", ActionEnum.LOGIN, LogStatus.FAILED, message);
    }
}
