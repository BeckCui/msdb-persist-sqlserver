package com.dhl.fin.api.common.exception;


import com.dhl.fin.api.common.dto.ApiResponseStatus;

/**
 * UserInfo friendly exception. IssueMailMessage will be exposed to end account, so do not include any technical details.
 */
public class UserFriendlyException extends BusinessException {
    private boolean needLocalize;
    private int status = ApiResponseStatus.ERROR;
    private Object[] messageArguments;

    public boolean isNeedLocalize() {
        return needLocalize;
    }

    private void setNeedLocalize(boolean needLocalize) {
        this.needLocalize = needLocalize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object[] getMessageArguments() {
        return messageArguments;
    }

    public void setMessageArguments(Object[] messageArguments) {
        this.messageArguments = messageArguments;
    }

    public UserFriendlyException(String messageSourceName) {
        setNeedLocalize(true);
    }

    public UserFriendlyException(String error, boolean needLocalize) {
        setNeedLocalize(needLocalize);
    }

    public UserFriendlyException(String messageSourceName, Object[] messageArguments) {
        setNeedLocalize(true);
        this.messageArguments = messageArguments;
    }

    public UserFriendlyException(String error, boolean needLocalize, int status) {
        setNeedLocalize(needLocalize);
        setStatus(status);
    }
}
