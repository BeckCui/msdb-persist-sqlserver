package com.dhl.fin.api.common.enums;

import com.dhl.fin.api.common.annotation.DictionaryEnum;
import com.dhl.fin.api.common.util.StringUtil;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author CuiJianbo
 * @date 2020.02.22
 */

@Getter
@DictionaryEnum(code = "actionType", name = "操作类型")
public enum ActionEnum {

    ADD("add", "新增"),

    UPDATE("update", "修改"),

    DELETE("delete", "删除"),

    EXPORT("export", "导出"),

    BATCH_DELETE("batchDelete", "批量删除"),

    BATCH_IMPORT("batchImport", "批量导入"),

    SELECT_DELETE("selectDelete", "多选删除"),

    PAGE("page", "分页查询"),

    LIST("list", "查询"),

    GET_DOMAIN("getDomain", "获取domain"),

    SEND_MAIL("sendMail", "发送邮件"),

    CONNECTION("connection", "连接"),

    IMPORT("import", "导入"),

    OTHER("other", "其他"),

    DOWNLOAD("download", "下载"),

    UPLOAD("upload", "上传"),

    LOGIN("login", "登录"),

    LOGIN_OUT("loginOut", "退出");

    private String code;
    private String name;

    ActionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    public static ActionEnum getByCode(String code) {
        if (StringUtil.isEmpty(code)) {
            return null;
        }
        return Arrays.stream(ActionEnum.values()).filter(p -> p.getCode().equals(code)).findFirst().orElse(null);
    }
}
