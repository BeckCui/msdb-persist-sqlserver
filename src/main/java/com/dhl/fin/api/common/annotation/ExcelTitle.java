package com.dhl.fin.api.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识excel导入导出的字段
 *
 * @author becui
 * @date 4/5/2020
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelTitle {

    //title 中文名字
    String name() default "";


    //title 英文名字
    String code() default "";


    //排序
    int sort() default -1;


    //列宽
    int width() default -1;

    //日期格式  yyyy-MM-dd HH:mm:ss
    String dateFormat() default "yyyy-MM-dd";

    //导入时候默认值
    String defaultV() default "";

    //数据字典
    String dictionary() default "-";

    //格式校验
    String formatCheck() default "";

    //是否允许为空
    boolean allowNull() default true;

    /**
     * 依据excelSheet context 的field 判断这个字段是否作为导入导出字段
     * true： 禁用
     * false：启用
     */
    String disable() default "";


}



