package com.mxny.ss.uid.constants;

import com.mxny.ss.uid.domain.BizNumberRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务号常量
 */
public class BizNumberConstant {
    /**
     * 业务规则缓存
     */
    public static Map<String, BizNumberRule> bizNumberCache = new ConcurrentHashMap<>();

}
