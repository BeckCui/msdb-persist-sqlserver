package com.dhl.fin.api.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by CuiJianbo on 2020.03.07.
 */
@Data
@Builder
public class OptionDto {
    private String name;
    private String value;
}
