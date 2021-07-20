package com.mxny.ss.retrofitful.aop.annotation;

import java.lang.annotation.*;

/**
 * 自定义restful接口的AOP切面拦截器排序
 */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
    int value() default 1;
}
