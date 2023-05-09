package com.dhl.fin.api.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 5/21/2020
 */
@Data
public class LoginUserPermissionDto implements Serializable {

    private Map dictionaries;
    private Map loginUser;
    private List projects;
    private List manageProjects;
    private Map menus;
    private Map roles;
    private Map actions;

}









