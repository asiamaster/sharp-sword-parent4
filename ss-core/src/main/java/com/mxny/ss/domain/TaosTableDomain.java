package com.mxny.ss.domain;

import com.mxny.ss.dto.ITaosTableDomain;

import javax.persistence.Transient;

/**
 * 基础实体类
 */
public class TaosTableDomain extends TaosDomain implements ITaosTableDomain {

    @Transient
    protected String dynamicTableName;

    @Transient
    protected Boolean containsTag;

    @Override
    public String getDynamicTableName() {
        return dynamicTableName;
    }

    void setDynamicTableName(String dynamicTableName){
        this.dynamicTableName = dynamicTableName;
    }

    @Override
    public Boolean getContainsTag() {
        return containsTag;
    }

    @Override
    public void setContainsTag(Boolean containsTag) {
        this.containsTag = containsTag;
    }

}
