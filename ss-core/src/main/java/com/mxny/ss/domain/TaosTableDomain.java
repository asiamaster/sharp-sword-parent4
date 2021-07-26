package com.mxny.ss.domain;

import com.mxny.ss.dto.ITaosTableDomain;

import javax.persistence.Transient;

/**
 * 基础实体类
 */
public class TaosTableDomain extends Domain<Long> implements ITaosTableDomain {

    @Transient
    protected Long ts;

    @Transient
    protected String dynamicTableName;

    @Override
    public Long getTs() {
        return ts;
    }
    @Override
    public void setTs(Long id) {
        this.ts = ts;
    }

    @Override
    public String getDynamicTableName() {
        return dynamicTableName;
    }

    void setDynamicTableName(String dynamicTableName){
        this.dynamicTableName = dynamicTableName;
    }
}
