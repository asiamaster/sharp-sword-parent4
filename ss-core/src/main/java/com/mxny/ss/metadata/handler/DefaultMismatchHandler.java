package com.mxny.ss.metadata.handler;

import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * 默认的值不匹配处理器
 * 用于批量提供者转化
 */
@Component
public class DefaultMismatchHandler implements Function {
    @Override
    public Object apply(Object o) {
        return o;
    }
}
