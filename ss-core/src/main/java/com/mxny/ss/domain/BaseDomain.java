package com.mxny.ss.domain;

import com.mxny.ss.dto.IBaseDomain;

import javax.persistence.Transient;

/**
 * 基础实体类
 */
public class BaseDomain extends Domain<Long> implements IBaseDomain {

    @Transient
    protected Long id;

    @Override
    public Long getId() {
        return id;
    }
    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
