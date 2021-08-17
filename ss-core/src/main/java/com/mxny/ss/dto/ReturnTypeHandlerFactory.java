package com.mxny.ss.dto;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 返回值类型转换工厂，用于解决DTOHandler过多的if判断
 */
public class ReturnTypeHandlerFactory {

    static final Map<Class<?>, Strategy> cache = new HashMap<>();

    static {
        cache.put(Long.class, new LongStrategy());
        cache.put(Integer.class, new IntegerStrategy());
        cache.put(Float.class, new FloatStrategy());
        cache.put(Double.class, new DoubleStrategy());
        cache.put(Date.class, new DateStrategy());
        cache.put(Boolean.class, new BooleanStrategy());
        cache.put(Byte.class, new ByteStrategy());
        cache.put(BigDecimal.class, new BigDecimalStrategy());
        cache.put(Clob.class, new ClobStrategy());
        cache.put(Instant.class, new InstantStrategy());
        cache.put(LocalDateTime.class, new LocalDateTimeStrategy());
        cache.put(LocalDate.class, new LocalDateStrategy());
        cache.put(List.class, new ListStrategy());
        cache.put(Set.class, new SetStrategy());
        cache.put(String.class, new StringStrategy());
        cache.put(Map.class, new MapStrategy());
        cache.put(IDTO.class, new IDTOStrategy());
    }

    /**
     * 根据类型，选择对应的策略器执行
     * @param type  要转换的类型
     * @param value 要转换的值
     * @return
     */
    public static Object convertValue(Class<?> type, Object value) {
        Strategy strategy = cache.get(type);
        if(strategy == null || value == null){
            return value;
        }
        if(type.isAssignableFrom(value.getClass())){
            return value;
        }
        try {
            return strategy.convert(value);
        } catch (Exception e) {
            //转换失败返原值
//            e.printStackTrace();
            return value;
        }
    }

    /**
     * 策略接口
     */
    public interface Strategy {
        /**
         * 类型转换
         * @param value
         * @return
         */
        Object convert(Object value);
    }

    /**
     * String转换策略
     */
    private static class StringStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return value.toString();
        }
    }

    /**
     * Map转换策略
     */
    private static class MapStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return JSONObject.parseObject(value.toString());
        }
    }

    /**
     * DTO转换策略
     */
    private static class IDTOStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(value instanceof IDTO){
                return (IDTO)value;
            }
            DTO dto = new DTO();
            dto.putAll(JSONObject.parseObject(value.toString()));
            return DTOUtils.proxy(dto, (Class)DTOUtils.getDTOClass(value));
        }
    }

    /**
     * List转换策略
     */
    private static class ListStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return value instanceof String ? Lists.newArrayList(value) : value.getClass().isArray() ? Lists.newArrayList(new Object[]{value}) :Lists.newArrayList((List)value);
        }
    }

    /**
     * Set转换策略
     */
    private static class SetStrategy implements Strategy{
        @Override
        public Object convert(Object value) {
            return value instanceof String ? Sets.newHashSet(value) : value.getClass().isArray() ? Sets.newHashSet(new Object[]{value}) :Sets.newHashSet((List)value);
        }
    }


    /**
     * 长整型转换策略
     */
    private static class LongStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Long.parseLong(value.toString());
        }
    }

    /**
     * 整型转换策略
     */
    private static class IntegerStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Integer.parseInt(value.toString());
        }
    }

    /**
     * Float转换策略
     */
    private static class FloatStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Float.parseFloat(value.toString());
        }
    }

    /**
     * Double转换策略
     */
    private static class DoubleStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Double.parseDouble(value.toString());
        }
    }

    /**
     * Byte转换策略
     */
    private static class ByteStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Byte.parseByte(value.toString());
        }
    }

    /**
     * Boolean转换策略
     */
    private static class BooleanStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:Boolean.parseBoolean(value.toString());
        }
    }

    /**
     * BigDecimal转换策略
     */
    private static class BigDecimalStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:new BigDecimal(value.toString());
        }
    }

    /**
     * Clob转换策略
     */
    private static class ClobStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            return StringUtils.isBlank(value.toString())?null:getClobString((java.sql.Clob)value);
        }
    }

    /**
     * Instant转换策略
     */
    private static class InstantStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            if(String.class.equals(value.getClass())){
                String format = "yyyy-MM-dd HH:mm:ss";
                if(((String)value).length() == 10){
                    format = "yyyy-MM-dd";
                }else if(((String)value).length() == 23){
                    format = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                return Instant.from(DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault()).parse((String)value));
            } else if(Long.class.equals(value.getClass())){
                return Instant.ofEpochMilli((Long) value);
            }
            return null;
        }
    }

    /**
     * LocalDateTime转换策略
     */
    private static class LocalDateTimeStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            if(String.class.equals(value.getClass())){
                String format = "yyyy-MM-dd HH:mm:ss";
                if(((String)value).length() == 10){
                    format = "yyyy-MM-dd";
                }else if(((String)value).length() == 23){
                    format = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                return LocalDateTime.parse((String) value, DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault()));
            }else if(Long.class.equals(value.getClass())){
                return LocalDateTime.ofInstant(Instant.ofEpochMilli((Long) value), ZoneId.systemDefault());
            }
            return null;
        }
    }

    /**
     * LocalDate转换策略
     */
    private static class LocalDateStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            if(String.class.equals(value.getClass())){
                String format = "yyyy-MM-dd HH:mm:ss";
                if(((String)value).length() == 10){
                    format = "yyyy-MM-dd";
                }else if(((String)value).length() == 23){
                    format = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                return LocalDate.parse((String) value, DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault()));
            }
            return null;
        }
    }

    /**
     * Date转换策略
     */
    private static class DateStrategy implements Strategy{

        @Override
        public Object convert(Object value) {
            if(StringUtils.isBlank(value.toString())){
                return null;
            }
            // 如果当前字段的值不是日期型, 转换返回值，并且将新的返回值填入委托对象中
            if(String.class.equals(value.getClass())){
                try {
                    return StringUtils.isNumeric(value.toString()) ? new Date(Long.parseLong(value.toString())) : DateUtils.parseDate(value.toString(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss.SSS", "E MMM dd yyyy HH:mm:ss");
                } catch (ParseException e) {
                    try {
                        return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss", Locale.US).parse(value.toString());
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (Long.class.equals(value.getClass())) {
                return new Date((Long)value);
            }
            return null;
        }
    }

    private static String getClobString(java.sql.Clob c) {
        try {
            Reader reader=c.getCharacterStream();
            if (reader == null) {
                return null;
            }
            StringBuffer sb = new StringBuffer();
            char[] charbuf = new char[4096];
            for (int i = reader.read(charbuf); i > 0; i = reader.read(charbuf)) {
                sb.append(charbuf, 0, i);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

}
