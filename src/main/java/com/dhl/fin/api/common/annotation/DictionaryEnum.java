package com.dhl.fin.api.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用：将Enum写成dictionary存入数据库
 *
 * @author becui
 * @date 4/5/2020
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DictionaryEnum {

    //数据字典属于哪个project
    String projectCode() default "";

    //数据字典的parent code
    String code() default "";

    //数据字典的parent 名字
    String name() default "";


}
