package com.mxny.ss.dto;

/**
 * 基础实体类
 */
public interface IBaseDomain extends IDomain<Long> {

    String ID = "id";

    Long getId();
    void setId(Long id);
}
