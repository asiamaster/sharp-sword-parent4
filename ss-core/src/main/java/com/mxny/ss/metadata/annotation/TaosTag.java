package com.mxny.ss.metadata.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 标记TDEngine的Tag
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface TaosTag {

    /**
     * 是否超级表Tag
     * @return
     */
    boolean value() default true;
}