package com.dhl.fin.api.common.util.excel;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ReflectUtil;
import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by CuiJianbo on 2020.03.22.
 */

public class ExcelSheetBuilder {

    private ExcelSheet excelSheet = new ExcelSheet();
    private Map<String, Object> context = new HashMap<>();

    public ExcelSheetBuilder addRowList(List rows) {
        excelSheet.getRowList().addAll(rows);
        return this;
    }

    public ExcelSheetBuilder addRow(Object row) {
        excelSheet.getRowList().add(row);
        return this;
    }

    public ExcelSheetBuilder addTitle(String key, String name, String dicCode, Integer sort, Integer width) {

        int sortNum = ObjectUtil.isNull(sort) ? excelSheet.getTitles().size() + 1 : sort;

        excelSheet.getTitles().add(
                ExcelTitleBean
                        .builder()
                        .dicCode(ObjectUtil.notNull(dicCode) ? dicCode : null)
                        .width(ObjectUtil.notNull(width) ? width : -1)
                        .sort(sortNum)
                        .key(key)
                        .name(name)
                        .build()
        );
        return this;
    }

    public ExcelSheetBuilder addTitle(String key, String name) {
        return addTitle(key, name, null, null, null);
    }

    public ExcelSheetBuilder addTitle(String key, String name, Integer width) {
        return addTitle(key, name, null, null, width);
    }

    public ExcelSheetBuilder addTitle(String key, String name, boolean addWhen) {
        if (addWhen) {
            return this.addTitle(key, name);
        }
        return this;
    }


    public ExcelSheetBuilder addTitles(List<ExcelTitleBean> excelTitle) {
        for (ExcelTitleBean excelTitleBean : excelTitle) {
            addTitle(excelTitleBean);
        }
        return this;
    }

    public ExcelSheetBuilder addTitle(ExcelTitleBean excelTitle) {

        int sort = excelTitle.getSort();
        if (sort <= 0) {
            int sortNum = excelSheet.getTitles().size() + 1;
            excelTitle.setSort(sortNum);
        }
        excelSheet.getTitles().add(excelTitle);
        return this;
    }


    public ExcelSheetBuilder addProperty(String key, Object value) {
        if (!StringUtil.isEmpty(key)) {
            context.put(key, value);
        }
        return this;
    }

    public ExcelSheetBuilder setTitle(Class entityClass) {
        Arrays.stream(ReflectUtil.getFields(entityClass))
                .map(p -> {
                            Object o = p.getDeclaredAnnotation(ExcelTitle.class);
                            if (ObjectUtil.isNull(o)) {
                                return null;
                            } else {
                                String v = p.getDeclaredAnnotation(ExcelTitle.class).disable();
                                boolean isDisable = MapUtil.getBoolean(context, v, false);
                                if (isDisable) {
                                    return null;
                                } else {
                                    return StringUtil.join(((ExcelTitle) o).sort(), ",", ((ExcelTitle) o).name(), ",", ((ExcelTitle) o).code(), ",", p.getName(), ",", ((ExcelTitle) o).dictionary(), ",", ((ExcelTitle) o).width());
                                }
                            }
                        }
                )
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(p -> Integer.valueOf(p.split(",")[0])))
                .forEach(p -> {
                    String[] titleMate = p.split(",");
                    String titleName = titleMate[1];
                    String titleCode = titleMate[2];
                    String fieldName = titleMate[3];
                    String dictionary = titleMate[4];
                    int width = Integer.valueOf(titleMate[5]);
                    String code = StringUtil.isEmpty(titleCode) ? StringUtil.toUnderlineCase(fieldName) : titleCode;
                    addTitle(ExcelTitleBean.builder()
                            .key(code)
                            .name(titleName)
                            .dicCode(dictionary)
                            .width(width)
                            .build()
                    );
                });
        return this;
    }

    /*public ExcelSheetBuilder removeTitleWhen(boolean removeWhen, String... key) {
        return removeWhen ? removeTitle(key) : this;
    }*/

    public ExcelSheetBuilder removeTitle(String... key) {
        List<String> underLineCodes = Arrays.stream(key).map(StringUtil::toUnderlineCase).collect(Collectors.toList());
        String keys = StringUtil.join("," + CollectionUtil.join(underLineCodes, ",") + ",");
        List<ExcelTitleBean> newTiles = excelSheet.getTitles()
                .stream()
                .filter(p -> !keys.contains(p.getKey()))
                .collect(Collectors.toList());
        excelSheet.setTitles(newTiles);
        return this;
    }

    public ExcelSheetBuilder setSheetName(String sheetName) {
        excelSheet.setSheetName(sheetName);
        return this;
    }

    public ExcelSheetBuilder setHeadName(String headName) {
        excelSheet.setHeadName(headName);
        return this;
    }


    public ExcelSheetBuilder setDB(CommonDBService commonDBService) {
        excelSheet.setCommonDBService(commonDBService);
        return this;
    }

    public ExcelSheetBuilder setSql(String sql) {
        excelSheet.setSql(sql);
        return this;
    }

    public ExcelSheetBuilder setPageSize(int pageSize) {
        excelSheet.setPageSize(pageSize);
        return this;
    }


    public ExcelSheet build() {
        return this.excelSheet;
    }

}




