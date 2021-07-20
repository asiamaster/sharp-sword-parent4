package com.mxny.ss.oplog.base;

import com.mxny.ss.oplog.dto.LogContext;

import java.lang.reflect.Method;

/**
 * 日志内容提供者
 */
public interface LogContentProvider {
    /**
     * 获取日志内容
     * 有异常需要返回null
     * @param method    调用的方法
     * @param args      方法的参数
     * @param params    OpLog注解中的params
     * @param logContext    环境信息
     * @return
     */
    String content(Method method, Object[] args, String params, LogContext logContext);
}
