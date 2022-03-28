package com.mxny.ss.dao.wrapper;

import com.mxny.ss.util.POJOUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;

import java.util.Map;

/**
 * 利刃的Map查询结果转换
 * @author: WangMi
 * @time: 2021/10/26 11:29
 */
public class SsMapWrapper extends MapWrapper {

    public SsMapWrapper(MetaObject metaObject, Map<String, Object> map) {
        super(metaObject, map);
    }


    @Override
    public String findProperty(String name, boolean useCamelCaseMapping) {
        if(useCamelCaseMapping){
            //这一行代码实现将map所有key值下划线转驼峰
            return name==null?"": POJOUtils.lineToHump(name);
        }
        return name;
    }

}