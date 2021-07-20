package com.mxny.ss.retrofitful.annotation;

import java.lang.annotation.*;

/**
 * 请求头，只能作为Map<String, String>参数的注解
 * Created by asiamastor on 2016/11/28.
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReqHeader {
    boolean required() default true;
}
