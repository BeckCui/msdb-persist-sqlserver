package com.dhl.fin.api.common.util.csv;


import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;


/**
 * 待开发
 */
@Getter
@Setter
public class CsvUtil {

    private String fileName;

    private List rowList = new LinkedList();

    private List<CsvTitleBean> titles = new LinkedList<>();

    public static CsvBuilder builder() {
        return new CsvBuilder();
    }


}
