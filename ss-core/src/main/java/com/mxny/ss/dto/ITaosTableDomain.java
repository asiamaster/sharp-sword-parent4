package com.mxny.ss.dto;

import tk.mybatis.mapper.entity.IDynamicTableName;

import javax.persistence.Transient;
import java.util.Set;

/**
 * TDengine子表实体
 */
public interface ITaosTableDomain extends ITaosDomain, IDynamicTableName {

    /**
     * 是否包含Tag(在查询和新增中过是否包含tag字段)
     * @return
     */
    @Transient
    Boolean getContainsTag();
    void setContainsTag(Boolean containsTag);

    /**
     * select和where之间自定义SQL块，替换原有的select块, 用于ExpandSelect
     * 用法如: select ${selectColumns} from 或自己添加子查询等
     * 这里用于区分是否在查询中添加@TaosTag注解的字段
     * @return
     */
    @Transient
    Set<String> getSelectColumns();
    void setSelectColumns(Set<String> selectColumns);
}
