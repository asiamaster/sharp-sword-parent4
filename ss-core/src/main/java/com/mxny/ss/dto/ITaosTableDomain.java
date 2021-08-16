package com.mxny.ss.dto;

import tk.mybatis.mapper.entity.IDynamicTableName;

import javax.persistence.Transient;

/**
 * TDengine子表实体
 */
public interface ITaosTableDomain extends ITaosDomain, IDynamicTableName {

    /**
     * 是否包含Tag(在查询和新增中过是否包含tag字段)
     * 用于在listByExample的查询字段和查询条件中判断，是否需要加入@TaosTag注解的字段，默认会处理@TaosTag注解的字段
     * 如不处理@TaosTag注解的字段，需要手动设置为false
     * @return
     */
    @Transient
    Boolean getContainsTag();
    void setContainsTag(Boolean containsTag);

}
