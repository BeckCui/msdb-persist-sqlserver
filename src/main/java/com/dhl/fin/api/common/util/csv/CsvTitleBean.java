package com.dhl.fin.api.common.util.csv;

import com.dhl.fin.api.common.enums.ExcelDataType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvTitleBean {
    private String key;
    private String name;
    private ExcelDataType dataType;
    private int width;
    private int sort;
}
