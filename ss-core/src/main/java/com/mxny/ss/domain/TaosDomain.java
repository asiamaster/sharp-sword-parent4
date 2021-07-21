package com.mxny.ss.domain;

import com.mxny.ss.dto.ITaosDomain;

import javax.persistence.Transient;

/**
 * 基础实体类
 */
public class TaosDomain extends Domain<Long> implements ITaosDomain {

    @Transient
    protected Long ts;

    @Override
    public Long getTs() {
        return ts;
    }
    @Override
    public void setTs(Long id) {
        this.ts = ts;
    }
}
