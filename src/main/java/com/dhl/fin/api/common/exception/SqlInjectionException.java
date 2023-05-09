package com.dhl.fin.api.common.exception;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;

/**
 * Created by CuiJianbo on 2020.03.03.
 */

public class SqlInjectionException extends BusinessException {

    public SqlInjectionException() {
        super("无", ActionEnum.OTHER, LogStatus.FAILED, "此请求存在sql注入风险");
    }

}
