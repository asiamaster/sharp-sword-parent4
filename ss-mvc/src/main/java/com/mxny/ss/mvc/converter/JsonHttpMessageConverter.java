package com.mxny.ss.mvc.converter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 自定义fastJson日期转换格式
 * 将默认的"yyyy-MM-dd HH:mm:ss"格式改为 "yyyy-MM-dd"格式
 * @author wm
 *
 */
public class JsonHttpMessageConverter extends FastJsonHttpMessageConverter{

    private static SerializeConfig mapping = new SerializeConfig();
    private static String dateFormat;
    static {
        dateFormat = "yyyy-MM-dd HH:mm:ss";
        mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
        mapping.put(Timestamp.class, new SimpleDateFormatSerializer(dateFormat));
        mapping.put(LocalDateTime.class, LocalDateTimeSerializer.instance);
        mapping.put(LocalDate.class, LocalDateSerializer.instance);
    }

    @Override
    protected void writeInternal(Object obj, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        OutputStream out = outputMessage.getBody();
        String text = JSON.toJSONString(obj, mapping, this.getFeatures());
        byte[] bytes = text.getBytes(this.getCharset());
        out.write(bytes);
    }

}