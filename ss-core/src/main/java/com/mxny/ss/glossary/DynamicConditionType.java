package com.mxny.ss.glossary;

/**
 * 动态实体条件类型
 */
public enum DynamicConditionType {
    CONDITION(1,"查询条件"),
    SUFFIX(2,"查询后缀"),
    SORT(3,"排序");

    private String name;
    private Integer code ;

    DynamicConditionType(Integer code, String name){
        this.code = code;
        this.name = name;
    }

    public static DynamicConditionType getOnlineState(Integer code) {
        switch (code) {
            case 1: return DynamicConditionType.CONDITION;
            case 2: return DynamicConditionType.SUFFIX;
            case 3: return DynamicConditionType.SORT;
            default:  return null;
        }
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
