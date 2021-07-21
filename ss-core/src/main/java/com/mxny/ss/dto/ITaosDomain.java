package com.mxny.ss.dto;

/**
 * TDengine数据库基础实体类
 * 主键名为ts，其它和BaseDomain一致
 */
public interface ITaosDomain extends IDomain<Long> {

    String ID = "ts";

    Long getTs();
    void setTs(Long ts);
}
