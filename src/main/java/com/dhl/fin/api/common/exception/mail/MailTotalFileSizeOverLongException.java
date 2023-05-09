package com.dhl.fin.api.common.exception.mail;

import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.exception.BusinessException;

/**
 * @author becui
 * @date 7/29/2020
 */
public class MailTotalFileSizeOverLongException extends BusinessException {


    public MailTotalFileSizeOverLongException() {
        super(null, ActionEnum.SEND_MAIL, LogStatus.FAILED, "邮件的附件总和大小不能超过10M");
    }

}


