package com.dhl.fin.api.common.dto;

import com.dhl.fin.api.common.service.LoginUserService;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.WebUtil;
import lombok.Builder;
import lombok.Data;

import java.util.*;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 4/21/2020
 */
@Builder
@Data
public class UserInfo implements Serializable {

    private Integer id;

    private String userName;

    private String uuid;

    private String adminType;

    private Integer status;

    private String email;

    private String defaultRoute;

    public List<Map> getRoles() {
        LoginUserService loginUserService = SpringContextUtil.getBean(LoginUserService.class);
        String appcode = SpringContextUtil.getPropertiesValue("custom.projectCode");


        String[] t = appcode.split("_");
        if (t.length > 1) {
            appcode = t[1];
        } else {
            appcode = t[0];
        }

        String projectCode = appcode;

        return loginUserService
                .getRoles(uuid)
                .stream()
                .filter(l -> MapUtil.getString(l, "projectCode").equals(projectCode))
                .map(p -> MapUtil.builder()
                        .add("code", MapUtil.getString(p, "roleCode"))
                        .add("name", MapUtil.getString(p, "roleName"))
                        .build())
                .collect(Collectors.toList());
    }

    public ActiveRole getActiveRole() {
        String activeRoleCode = WebUtil.getStringParam("activeRoleCode");
        String activeRoleName = WebUtil.getStringParam("activeRoleName");
        return ActiveRole.builder()
                .code(activeRoleCode)
                .name(activeRoleName)
                .build();
    }

}


