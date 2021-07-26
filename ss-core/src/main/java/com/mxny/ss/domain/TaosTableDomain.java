package com.mxny.ss.domain;

import com.mxny.ss.dto.ITaosTableDomain;

import javax.persistence.Transient;
import java.util.Set;

/**
 * 基础实体类
 */
public class TaosTableDomain extends Domain<Long> implements ITaosTableDomain {

    @Transient
    protected Long ts;

    @Transient
    protected String dynamicTableName;

    @Transient
    protected Boolean containsTag;

    @Transient
    protected Set<String> selectColumns;

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

    @Override
    public Boolean getContainsTag() {
        return containsTag;
    }

    @Override
    public void setContainsTag(Boolean containsTag) {
        this.containsTag = containsTag;
    }

    @Override
    public Set<String> getSelectColumns() {
        return selectColumns;
    }

    @Override
    public void setSelectColumns(Set<String> selectColumns) {
        this.selectColumns = selectColumns;
    }
}
