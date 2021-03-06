package com.mxny.ss.retrofitful.annotation;

import java.lang.annotation.*;

/**
 * VO字段
 * Created by asiamastor on 2016/11/28.
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface VOField {
    String value();
    boolean required() default true;
}
