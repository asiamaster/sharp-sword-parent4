package com.mxny.ss.retrofitful.annotation;

import java.lang.annotation.*;

/**
 * Created by asiamastor on 2016/11/28.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {
    String value() default "";
}
