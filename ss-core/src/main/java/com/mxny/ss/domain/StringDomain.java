package com.mxny.ss.domain;

import com.mxny.ss.dto.IStringDomain;

/**
 * 基础实体类
 */
public class StringDomain extends Domain<String> implements IStringDomain {

    @Override
    public String getId() {
        return id;
    }
    @Override
    public void setId(String id) {
        this.id = id;
    }
}
