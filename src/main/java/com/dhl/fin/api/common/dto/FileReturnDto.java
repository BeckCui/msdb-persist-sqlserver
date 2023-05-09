package com.dhl.fin.api.common.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author becui
 * @date 5/6/2020
 */
@Data
@Builder
public class FileReturnDto {
    private String fileName;
    private String directory;
}
