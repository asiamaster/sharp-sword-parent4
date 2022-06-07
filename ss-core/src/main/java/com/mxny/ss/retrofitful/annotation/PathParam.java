package com.mxny.ss.retrofitful.annotation;

import java.lang.annotation.*;

/**
 * 路径参数
 * Created by asiamastor on 2022/06/07.
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam {
    String value();
    boolean required() default true;
}
