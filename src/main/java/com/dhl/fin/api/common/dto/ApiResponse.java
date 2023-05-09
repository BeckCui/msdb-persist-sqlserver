package com.dhl.fin.api.common.dto;

import com.dhl.fin.api.common.enums.MsgTypeEnum;
import com.dhl.fin.api.common.enums.NotifyTypeEnum;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;


/**
 * @author becui
 * @date 5/21/2020
 */
@Data
public class ApiResponse<T> implements Serializable {
    private int code;
    private T data;
    private String msg;
    private String notifyType;
    private String msgType;

    public ApiResponse() {
    }

    public ApiResponse(int status, T result, String msg) {
        setCode(status);
        setData(result);
        setMsg(msg);
    }

    public ApiResponse(int status, T result, String msg, NotifyTypeEnum notifyTypeEnum, MsgTypeEnum msgTypeEnum) {
        setCode(status);
        setData(result);
        setMsg(msg);
        setNotifyType(notifyTypeEnum.getCode());
        setMsgType(msgTypeEnum.getCode());
    }

    public ApiResponse(T result) {
        setCode(ApiResponseStatus.SUCCESS);
        setData(result);
        setMsg(null);
    }

    public static ApiResponse success(Object result) {
        return new ApiResponse(result);
    }

    public static ApiResponse success() {
        return new ApiResponse("success");
    }

    public static ApiResponse error() {
        return new ApiResponse(ApiResponseStatus.ERROR, null, "后台异常", NotifyTypeEnum.NOTIFY, MsgTypeEnum.ERROR);
    }

    public static ApiResponse error(String error) {
        return error(StringUtils.isEmpty(error) ? "后台异常" : error, NotifyTypeEnum.NOTIFY);
    }

    public static ApiResponse error(String info, NotifyTypeEnum notifyTypeEnum, MsgTypeEnum msgTypeEnum) {
        return new ApiResponse(ApiResponseStatus.ERROR, null, StringUtils.isEmpty(info) ? "后台异常" : info, notifyTypeEnum, msgTypeEnum);
    }

    public static ApiResponse error(String error, Object result) {
        return new ApiResponse(ApiResponseStatus.ERROR, result, StringUtils.isEmpty(error) ? "后台异常" : error, NotifyTypeEnum.NOTIFY, MsgTypeEnum.ERROR);
    }

    public static ApiResponse info(String info) {
        return info(info, NotifyTypeEnum.MESSAGE, MsgTypeEnum.INFO);
    }

    public static ApiResponse info(String info, NotifyTypeEnum notifyTypeEnum, MsgTypeEnum msgTypeEnum) {
        return new ApiResponse(ApiResponseStatus.INFO, null, StringUtils.isEmpty(info) ? "后台异常" : info, notifyTypeEnum, msgTypeEnum);
    }


}
