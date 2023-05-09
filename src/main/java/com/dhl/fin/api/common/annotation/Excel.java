package com.dhl.fin.api.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识domain是Excel导出到处的实体对象
 *
 * @author becui
 * @date 4/5/2020
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Excel {

    //文件 名字
    String value() default "";


}
