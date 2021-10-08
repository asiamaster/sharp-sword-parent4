package com.mxny.ss.dto;

/**
 * TAOS字段描述
 * @author: WangMi
 * @time: 2021/9/29 9:45
 */
public interface TaosFieldDescribe extends IDTO {

    String getField();
    void setField(String field);

    String getType();
    void setType(String type);

    Integer getLength();

    void setLength(Integer length);

    String getNote();
    void setNote(String note);
}
