package com.mxny.ss.domain;

import com.mxny.ss.dto.IBaseDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;

import javax.persistence.*;

/**
 * 动态字段
 */
@Table(name = "dynamic_field")
public interface DynamicField extends IBaseDomain {

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @FieldDef(label="主键")
    @EditMode(editor = FieldEditor.Number, required = true)
    Long getId();

    @Override
    void setId(Long id);

    /**
     * 是否TaosTag, 1:是, 0:否
     * @return
     */
    @Column(name="is_tag")
    Boolean getIsTag();
    void setIsTag(Boolean isTag);
    /**
     * 表名
     * @return
     */
    @Column(name="table_name")
    String getTableName();

    void setTableName(String tableName);

    /**
     * 字段名
     * @return
     */
    @Column(name="field_name")
    String getFieldName();

    void setFieldName(String fieldName);

    /**
     * 数据库列名
     * @return
     */
    @Column(name="column_name")
    String getColumnName();

    void setColumnName(String columnName);

    /**
     * 字段类全名
     * @return
     */
    @Column(name="field_type")
    String getFieldType();

    void setFieldType(String fieldType);

    /**
     * 数据库类型
     * @return
     */
    @Column(name="data_type")
    String getDataType();

    void setDataType(String dataType);

    /**
     * 数据长度
     * @return
     */
    @Column(name="data_length")
    Integer getDataLength();

    void setDataLength(Integer dataLength);

    /**
     * 函数名
     * @return
     */
    @Column(name="func")
    String getFunc();
    void setFunc(String func);

    /**
     * 别名
     * @return
     */
    @Column(name="alias")
    String getAlias();
    void setAlias(String alias);

    /**
     * 业务编码
     * 默认'defaults'是所有字段信息
     * asc or desc
     * @return
     */
    @Column(name="business_code")
    String getBusinessCode();
    void setBusinessCode(String businessCode);

    /**
     * 是否启用
     * 1： 启用，0：禁用
     * @return
     */
    Boolean getEnabled();
    void setEnabled(Boolean enabled);

    /**
     * 备注
     * @return
     */
    @Column(name="notes")
    String getNotes();
    void setNotes(String notes);
}
