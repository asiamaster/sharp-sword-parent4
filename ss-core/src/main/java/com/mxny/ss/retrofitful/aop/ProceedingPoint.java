package com.mxny.ss.retrofitful.aop;

import java.lang.reflect.Method;

/**
 * 切点参数
 */
public interface ProceedingPoint {

    Method getMethod();

    void setMethod(Method method);

    Object[] getArgs();

    void setArgs(Object[] args);

    Object proceed() throws Throwable;

    Object proceed(Object[] args) throws Throwable;
}
