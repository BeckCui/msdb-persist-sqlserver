package com.dhl.fin.api.common.exception.mail;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.util.StringUtil;

/**
 * @author becui
 * @date 9/28/2020
 */
public class MailSendFailedException extends BusinessException {

    public MailSendFailedException(String msg) {
        super(null, ActionEnum.SEND_MAIL, LogStatus.FAILED, StringUtil.isEmpty(msg) ? "邮件发送失败" : msg);
    }


}


