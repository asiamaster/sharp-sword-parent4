package com.mxny.ss.processor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author asiam
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface GenDTOMethod {

    /**
     * 是否生成JPA注释
     * @return
     */
    boolean jpaAnnotation() default false;

    /**
     * 链式setter(返回当前对象)
     * @return
     */
    boolean chainSetter() default false;
}
