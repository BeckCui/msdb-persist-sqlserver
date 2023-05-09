package com.dhl.fin.api.common.exception;

import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageAlert extends RuntimeException {

    private MsgTypeEnum msgTypeEnum;

    private NotifyTypeEnum notifyTypeEnum;

    private String message;

    public MessageAlert(String message) {
        this.message = message;

        this.msgTypeEnum = MsgTypeEnum.ERROR;

        this.notifyTypeEnum = NotifyTypeEnum.MESSAGE;
    }

    public MessageAlert(String message, MsgTypeEnum msgTypeEnum, NotifyTypeEnum notifyTypeEnum) {
        this.message = message;

        this.msgTypeEnum = msgTypeEnum;

        this.notifyTypeEnum = notifyTypeEnum;

    }

}
