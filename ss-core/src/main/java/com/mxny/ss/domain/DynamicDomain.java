package com.mxny.ss.domain;

import com.mxny.ss.dto.DTOUtils;
import com.mxny.ss.dto.IDynamicResultType;
import com.mxny.ss.dto.IMybatisForceParams;
import com.mxny.ss.dto.ITaosTableDomain;
import com.mxny.ss.metadata.FieldEditor;
import com.mxny.ss.metadata.annotation.EditMode;
import com.mxny.ss.metadata.annotation.FieldDef;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;
import java.util.Map;

/**
 * TAOS动态模型，包括超表名，子表名，动态字段，动态条件和动态查询列等信息
 * 子类需要有Tag相关字段
 */
public interface DynamicDomain extends ITaosTableDomain, IMybatisForceParams, IDynamicResultType {
    //动态表名字段名，用于批量插入数据行的动态表名
    String DYNAMIC_TABLE_NAME_KEY = "dynamicTableName";

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
     * 动态子表名
     * 当该方法返回值不为空时，就会使用返回值作为表名
     * @return
     */
    @Override
    @Transient
    String getDynamicTableName();

    void setDynamicTableName(String dynamicTableName);

    /**
     * 业务号
     * @return
     */
    @Transient
    String getBusinessCode();
    void setBusinessCode(String businessCode);

    /**
     * 动态字段
     * 用于查询结果和插入数据
     * @return
     */
    @Transient
    List<DynamicField> getDynamicFields();
    void setDynamicFields(List<DynamicField> dynamicFields);

    /**
     * 动态条件字段
     * 用于查询条件
     * @return
     */
    @Transient
    List<DynamicCondition> getDynamicConditions();
    void setDynamicConditions(List<DynamicCondition> dynamicConditions);

    /**
     * 动态条件字段
     * 用于查询条件
     * @return
     */
    @Transient
    List<SelectColumn> getSelectColumnList();
    void setSelectColumnList(List<SelectColumn> selectColumnList);

    /**
     * 每个字段对应的数据(包括tag)
     * key为DynamicField.fieldName
     * @return
     */
    @Transient
    List<Map> getDatas();
    void setDatas(List<Map> datas);

    static DynamicDomain create() {
        return DTOUtils.newInstance(DynamicDomain.class);
    }
}
