package com.mxny.ss.dto;

import java.beans.Transient;

/**
 * 动态返回DTO
 */
public interface IDynamicResultType {
    /**
     * 返回类型的完全限定名
     * @return
     */
    @Transient
    String getResultType();
    void setResultType(String resultType);
}
