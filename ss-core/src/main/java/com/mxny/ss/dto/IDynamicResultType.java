package com.mxny.ss.dto;

/**
 * 动态返回DTO
 */
public interface IDynamicResultType {
    /**
     * 返回类型的完全限定名
     * @return
     */
    String getResultType();
    void setResultType(String resultType);
}
