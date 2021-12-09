package com.mxny.ss.domain;

import com.mxny.ss.dto.IBaseDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;

import javax.persistence.*;
import java.util.Date;

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
     * 用于查询别名和插入数据时获取字段名
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

    /**
     * 排序号
     * @return
     */
    @Column(name="index")
    String getIndex();
    void setIndex(String index);

    /**
     * 创建人id
     * @return
     */
    Long getCreatorId();
    void setCreatorId(Long creatorId);

    /**
     * 创建时间
     * @return
     */
    Date getCreateTime();
    void setCreateTime(Date createTime);

    /**
     * 修改人id
     * @return
     */
    Long getModifierId();
    void setModifierId(Long modifierId);

    /**
     * 修改时间
     * @return
     */
    Date getModifyTime();
    void setModifyTime(Date modifyTime);
}
