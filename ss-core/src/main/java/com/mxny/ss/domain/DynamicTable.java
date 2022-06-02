package com.mxny.ss.domain;

import com.mxny.ss.dto.DTOUtils;
import com.mxny.ss.dto.IBaseDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "dynamic_table")
public interface DynamicTable extends IBaseDomain  {

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
     * 名称
     * @return
     */
    @Column(name="name")
    String getName();

    void setName(String name);

    /**
     * 备注
     * @return
     */
    @Column(name="notes")
    String getNotes();

    void setNotes(String notes);

    /**
     * 同步人id
     * @return
     */
    @Column(name="`sync_id`")
    Long getSyncId();
    void setSyncId(Long syncId);

    /**
     * 同步人名称
     * @return
     */
    @Column(name="`sync_name`")
    String getSyncName();
    void setSyncName(String syncName);

    /**
     * 同步时间
     * @return
     */
    @Column(name="`sync_time`")
    LocalDateTime getSyncTime();
    void setSyncTime(LocalDateTime syncTime);

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
    LocalDateTime getCreateTime();
    void setCreateTime(LocalDateTime createTime);

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
    LocalDateTime getModifyTime();
    void setModifyTime(LocalDateTime modifyTime);

    static DynamicTable create() {
        return DTOUtils.newInstance(DynamicTable.class);
    }

}
