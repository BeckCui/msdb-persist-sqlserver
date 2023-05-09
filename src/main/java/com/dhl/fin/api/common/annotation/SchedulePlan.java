package com.dhl.fin.api.common.annotation;


import com.dhl.fin.api.common.enums.TimeUnitEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 调度计划
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulePlan {

    //schedule 名字
    String name();


    //cron
    String cron() default "";

    //timeUnit
    TimeUnitEnum timeUnit() default TimeUnitEnum.DAY;

    //period
    long period() default 0;

    //如果出现异常就邮件通知，默认是给超级管理员发邮件
    String email() default "";


    //env
    //在哪个环境里执行（prod,dev,uat）
    String env() default "dev";


}
