package com.mxny.ss.dto;

/**
 * 基础实体类
 */
public interface IStringDomain extends IDomain<String> {

    String ID = "id";
    String getId();
    void setId(String id);
}
