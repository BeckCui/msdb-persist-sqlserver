package com.dhl.fin.api.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ActiveRole {
    private String code;
    private String name;
}
