package com.mxny.ss.mvc.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.mxny.ss.domain.BaseOutput;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author: WM
 * @time: 2021/10/27
 */
public class BaseOutputSerializer implements ObjectSerializer {
    public static final BaseOutputSerializer instance = new BaseOutputSerializer();

    public BaseOutputSerializer() {
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        } else {
            BaseOutput result = (BaseOutput) object;
            out.write(JSON.toJSONString(result, SerializerFeature.WRITE_MAP_NULL_FEATURES, SerializerFeature.WriteDateUseDateFormat , SerializerFeature.IgnoreErrorGetter));
            out.flush();
        }
    }
}
