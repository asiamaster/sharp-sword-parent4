package com.mxny.ss.seata.annotation;

import java.lang.annotation.*;

/**
 * 用于ResourceManager的分支事务注解
 * 比如: ezrestful RPC接口方法
 * TransactionManager的全局事务，请使用@GlobalTransaction注解
 */
@Inherited
@Target(value = ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalTx {

}
