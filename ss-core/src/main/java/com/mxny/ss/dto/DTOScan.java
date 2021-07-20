package com.mxny.ss.dto;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * DTO扫描
 * @author asiam
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({DTORegistrar.class})
public @interface DTOScan {
    /**
     * 扫描包
     * @return
     */
    String[] value() default {};

    String file() default "";
}
