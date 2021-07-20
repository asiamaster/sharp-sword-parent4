package com.mxny.ss.oplog.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 操作日志参数注解
 */
@Inherited
@Target(value = ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogParam {

    //绑定变量名称
    @AliasFor("value")
    String bindName() default "";

    //是否参数必填，必填时参数为空，则不记录日志
    boolean required() default false;

    //绑定变量名称
    @AliasFor("bindName")
    String value() default "";
}
