package com.mxny.ss.uid.service;

import com.mxny.ss.base.BaseService;
import com.mxny.ss.domain.BaseOutput;
import com.mxny.ss.uid.domain.BizNumberRule;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2020-01-21 14:38:55.
 */
public interface BizNumberRuleService extends BaseService<BizNumberRule, Long> {
    /**
     * 根据业务类型查询规则
     * @param type
     * @return
     */
    BizNumberRule getByType(String type);

    /**
     * 启/禁用
     * @param id
     * @param enable
     * @return
     */
    BaseOutput updateEnable(Long id, Boolean enable);
}