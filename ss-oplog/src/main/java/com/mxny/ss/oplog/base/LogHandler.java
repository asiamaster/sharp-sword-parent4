package com.mxny.ss.oplog.base;

import com.mxny.ss.oplog.dto.LogContext;

import java.lang.reflect.Method;

/**
 * 日志处理者
 */
public interface LogHandler {
    /**
     * 记录日志
     * @param content
     * @param method    调用的方法
     * @param args      方法的参数
     * @param params    OpLog注解中的params
     * @param logContext    环境信息
     * @param args
     */
    void log(String content, Method method, Object[] args, String params, LogContext logContext);
}
