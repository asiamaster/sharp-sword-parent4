package com.mxny.ss.oplog.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 操作日志方法注解
 */
@Inherited
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpLog {
    //内容模板
    @AliasFor("value")
    String template() default "";

    //内容模板
    @AliasFor("template")
    String value() default "";

    //日志处理器, 类或spring bean均可
    String handler() default "";

    //每次异步调用前,初始化日志环境变量。只支持spring bean
    String initializer() default "";

    //参数，如在参数中设置调用模块，可以在contentProvider和logHandler中获取
    String params() default "";

    //内容提供者，默认取模板为内容
    String contentProvider() default "";

    //必填表达式，用于校验参数中的属性，如: user.code
    //必填表达式将作为判断语句放在模板头部，判断为false将不显示模板内容
    String requiredExpr() default "";
}
