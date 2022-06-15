package com.mxny.ss.domain;

import com.mxny.ss.dto.DTOUtils;
import com.mxny.ss.dto.IBaseDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;

import javax.persistence.*;
import java.util.Date;

/**
 * 动态条件字段
 */
@Table(name = "dynamic_condition")
public interface DynamicCondition extends IBaseDomain {

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
     * 或者简名，参考ClassCache
     * @return
     */
    @Column(name="field_type")
    String getFieldType();

    void setFieldType(String fieldType);

    /**
     * 后缀函数名
     * @return
     */
    @Column(name="func")
    String getFunc();
    void setFunc(String func);

    /**
     * 字段值
     * @return
     */
    @Column(name="value")
    Object getValue();
    void setValue(Object value);

    /**
     * 条件类型.
     * 1: 查询条件， 2: 查询后缀(支持interval, sliding和 fill)， 3:排序
     * @return
     */
    @Column(name="type")
    Integer getType();
    void setType(Integer type);

    /**
     * 运算符
     * 参见@com.mxny.ss.domain.annotation.Operator
     * @return
     */
    @Column(name="operator")
    String getOperator();

    void setOperator(String operator);

    /**
     * like查询类型
     * 参见@com.mxny.ss.domain.annotation.Like
     * @return
     */
    @Column(name="like_type")
    String getLikeType();
    void setLikeType(String likeType);

    /**
     * 排序类型
     * asc or desc
     * @return
     */
    @Column(name="order_type")
    String getOrderType();
    void setOrderType(String orderType);

    /**
     * 业务编码
     * @return
     */
    @Column(name="business_code")
    String getBusinessCode();
    void setBusinessCode(String businessCode);

    /**
     * 排序号
     * @return
     */
    @OrderBy
    @Column(name="order_number")
    Integer getOrderNumber();
    void setOrderNumber(Integer orderNumber);

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

    static DynamicCondition create() {
        return DTOUtils.newInstance(DynamicCondition.class);
    }

}
