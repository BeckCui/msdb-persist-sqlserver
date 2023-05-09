package com.dhl.fin.api.common.enums;

import com.dhl.fin.api.common.annotation.DictionaryEnum;
import lombok.Getter;

/**
 * @author becui
 * @date 6/17/2020
 */

@Getter
@DictionaryEnum(code = "logStatus", name = "日志状态")
public enum LogStatus {

    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败");

    private String name;
    private String code;

    LogStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
