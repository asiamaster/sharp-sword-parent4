package com.mxny.ss.domain;

import com.mxny.ss.dto.IDTO;

import javax.persistence.Column;
import java.time.LocalDateTime;

/**
 * 通用的创建人和修改人相关字段
 * 请注意，使用TKMapper的时候，无法根据超类的字段生成SQL
 */
public interface CreateModify extends IDTO {

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
}
