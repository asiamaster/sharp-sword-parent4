package com.mxny.ss.mvc.converter;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: WM
 * @time: 2021/5/25 10:34
 */
public class LocalDateTimeSerializer implements ObjectSerializer {
    public static final LocalDateTimeSerializer instance = new LocalDateTimeSerializer();
    private static final String defaultPattern = "yyyy-MM-dd HH:mm:ss";

    SerializeConfig serializeConfig = SerializeConfig.globalInstance;


    public LocalDateTimeSerializer() {
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        } else {
            LocalDateTime result = (LocalDateTime) object;
            out.writeString(result.format(DateTimeFormatter.ofPattern(defaultPattern)));
        }
    }

}
