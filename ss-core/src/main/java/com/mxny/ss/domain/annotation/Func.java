package com.mxny.ss.domain.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * simple Aggregate function
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Func {
    /**
     * function name
     * @return
     */
    String value() default "";

    /**
     * alias name
     * @return
     */
    String alias() default "";
}
