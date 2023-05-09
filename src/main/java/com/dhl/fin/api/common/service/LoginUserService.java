package com.dhl.fin.api.common.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dhl.fin.api.common.dto.LoginUserPermissionDto;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author becui
 * @date 5/27/2020
 */
@Service
public class LoginUserService {


    @Autowired
    private RedisService redisService;

    @Value("${custom.projectCode}")
    private String projectCode;

    public Boolean loginUserIsManager() {
        String uuid = WebUtil.getLoginUser().getUuid();
        LoginUserPermissionDto loginUserPermissionDto = redisService.getUserPermission(uuid);
        Boolean isSuperManager = MapUtil.getBoolean(loginUserPermissionDto.getLoginUser(), "isSuperManager");
        if (isSuperManager) {
            return true;
        }
        Map roles = loginUserPermissionDto.getRoles();
        if (ObjectUtil.notNull(roles)) {
            JSONArray roleList = MapUtil.getJsonArray(roles, projectCode);
            if (CollectionUtil.isNotEmpty(roleList)) {
                Optional optional = roleList.stream().map(p -> ((JSONObject) p).get("code").toString()).filter(p -> p.endsWith("_sys_manager")).findFirst();
                return optional.isPresent();
            } else {
                return false;
            }
        }
        return false;
    }


    public List<Map> getRoles(String uuid) {

      /*  Acct acct = Acct.newBuilder().setUuid(uuid).build();

        List<Map> roles = fintpService.getUserRoles(acct)
                .getRolesList()
                .stream()
                .map(p -> MapUtil.builder()
                        .add("projectCode", p.getProjectCode())
                        .add("roleCode", p.getRoleCode())
                        .add("roleName", p.getRoleName())
                        .build()
                ).collect(Collectors.toList());

        return roles;

        */
        return null;
    }


}
