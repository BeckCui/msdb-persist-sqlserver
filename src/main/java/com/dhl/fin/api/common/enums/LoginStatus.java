package com.dhl.fin.api.common.enums;


import lombok.Getter;

@Getter
public enum LoginStatus {
    SUCCESS("success"),
    SERVERERROR("serverError"),
    VERIFICATIONFAILED("verificationFailed"),
    NOUSER("noUser"),
    LOGINTYPEERROR("loginTypeError"),
    VERIFICATIONCODEEXPIRED("verificationCodeExpired");

    private String status;

    LoginStatus(String status) {
        this.status = status;
    }

}
