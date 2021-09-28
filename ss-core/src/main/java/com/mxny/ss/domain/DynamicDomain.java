package com.mxny.ss.domain;

import com.mxny.ss.dto.IDynamicResultType;
import com.mxny.ss.dto.IMybatisForceParams;
import com.mxny.ss.dto.ITaosTableDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Map;

/**
 * TAOS动态模型
 * 子类需要有Tag相关字段
 */
@Table(name="dynamic_field")
public interface DynamicDomain extends ITaosTableDomain, IMybatisForceParams, IDynamicResultType {

    @Override
    @Id
    @KeySql(sql = "select 1")
    @Column(name = "ts")
    @FieldDef(label="ts")
    @EditMode(editor = FieldEditor.Datetime, required = true)
    Long getTs();

    @Override
    void setTs(Long ts);

    /**
     * 超级表名
     * @return
     */
    @Transient
    String getTableName();
    void setTableName(String tableName);

    /**
     * 当该方法返回值不为空时，就会使用返回值作为表名
     * @return
     */
    @Override
    @Transient
    String getDynamicTableName();

    void setDynamicTableName(String dynamicTableName);

    /**
     * 动态字段
     * 用于查询结果和插入数据
     * @return
     */
    @Transient
    Map<String, DynamicField> getDynamicFields();
    void setDynamicFields(Map<String, DynamicField> dynamicFields);

    /**
     * 动态条件字段
     * 用于查询条件
     * @return
     */
    @Transient
    Map<String, DynamicCondition> getDynamicConditions();
    void setDynamicConditions(Map<String, DynamicCondition> dynamicConditions);
}
