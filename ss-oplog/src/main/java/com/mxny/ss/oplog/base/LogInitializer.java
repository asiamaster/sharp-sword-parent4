package com.mxny.ss.oplog.base;

import com.mxny.ss.oplog.dto.LogContext;

import java.lang.reflect.Method;

/**
 * 日志初始化器
 */
public interface LogInitializer {
    /**
     * 每次异步调用前
     * 初始化日志环境变量LogContext
     * @param method
     * @param args
     * @return  返回LogContext用于内容提供者和日志处理器
     */
    LogContext init(Method method, Object[] args);

}
