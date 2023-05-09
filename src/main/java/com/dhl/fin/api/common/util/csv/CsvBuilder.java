package com.dhl.fin.api.common.util.csv;

import com.dhl.fin.api.common.util.ObjectUtil;

import java.util.List;

public class CsvBuilder {

    private CsvUtil csvUtil = new CsvUtil();

    public CsvUtil build() {
        return csvUtil;
    }

    public CsvBuilder addTitle(String key, String name) {
        return addTitle(key, name, null, null);
    }

    public CsvBuilder addTitle(String key, String name, Integer sort, Integer width) {

        int sortNum = ObjectUtil.isNull(sort) ? csvUtil.getTitles().size() + 1 : sort;
        csvUtil.getTitles().add(CsvTitleBean.builder()
                .key(key)
                .name(name)
                .sort(sortNum)
                .width(width)
                .build()
        );

        return this;
    }

    public CsvBuilder setFileName(String fileName) {
        csvUtil.setFileName(fileName);
        return this;
    }

    public CsvBuilder addRowList(List rows) {
        csvUtil.getRowList().addAll(rows);
        return this;
    }

}







