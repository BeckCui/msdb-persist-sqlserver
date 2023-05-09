package com.dhl.fin.api.common.dto;

/**
 * Api response status code
 */
public final class ApiResponseStatus {
    public static final int SUCCESS = 0;
    public static final int ERROR = -1;
    public static final int INFO = -2;
    public static final int UNAUTHORIZED = 1;
    public static final int INVALID_TOKEN = 2;

    public static final int NO_PERMISSION = 3;
    public static final int INVALID_TICKET = 4;
    public static final int TICKET_REQUIRED = 5;
    public static final int TOKEN_EXPIRED = 6;
    public static final int TICKET_EXPIRED = 7;
    public static final int APP_CREDENTIAL_REQUIRED = 8;
    public static final int APP_CREDENTIAL_NOT_EXIST_OR_ACTIVE = 9;
    public static final int APP_CREDENTIAL_INVALID_SIGNATURE = 10;
    public static final int APP_CREDENTIAL_SIGNATURE_REQUIRED = 11;
    public static final int APP_CREDENTIAL_INVALID_SECRET = 12;
    public static final int APP_CREDENTIAL_SIGNATURE_EXPIRED = 13;
    public static final int SERVER_ERROR = 500;
    public static final int VERIFICATION_FAILED = 90;
}