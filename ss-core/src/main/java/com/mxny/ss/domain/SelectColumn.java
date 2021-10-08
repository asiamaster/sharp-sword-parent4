package com.mxny.ss.domain;

import com.mxny.ss.dto.IBaseDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;

import javax.persistence.*;
import java.util.Date;

/**
 * 查询列
 */
@Table(name = "select_column")
public interface SelectColumn extends IBaseDomain {

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
     * 数据库列名
     * @return
     */
    @Column(name="column_name")
    String getColumnName();

    void setColumnName(String columnName);

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
