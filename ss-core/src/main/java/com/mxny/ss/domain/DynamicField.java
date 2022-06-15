package com.mxny.ss.domain;

import com.mxny.ss.dto.DTOUtils;
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

    @Column(name = "`dynamic_table_id`")
    @FieldDef(label="动态表id")
    Long getDynamicTableId();

    void setDynamicTableId(Long dynamicTableId);

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
    @Column(name="enabled")
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
    @Column(name="`order_number`")
    Integer getOrderNumber();
    void setOrderNumber(Integer orderNumber);

    /**
     * 创建人id
     * @return
     */
    @Column(name="`creator_id`")
    Long getCreatorId();
    void setCreatorId(Long creatorId);

    /**
     * 创建人名称
     * @return
     */
    @Column(name="`creator_name`")
    String getCreatorName();
    void setCreatorName(String creatorName);

    /**
     * 创建时间
     * @return
     */
    @Column(name="`create_time`")
    Date getCreateTime();
    void setCreateTime(Date createTime);

    /**
     * 修改人id
     * @return
     */
    @Column(name="`modifier_id`")
    Long getModifierId();
    void setModifierId(Long modifierId);

    /**
     * 修改人名称
     * @return
     */
    @Column(name="`modifier_name`")
    String getModifierName();
    void setModifierName(String modifierName);

    /**
     * 修改时间
     * @return
     */
    @Column(name="`modify_time`")
    Date getModifyTime();
    void setModifyTime(Date modifyTime);

    static DynamicField create() {
        return DTOUtils.newInstance(DynamicField.class);
    }
}
