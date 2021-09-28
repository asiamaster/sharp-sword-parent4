package com.mxny.ss;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 类缓存
 * @author: WangMi
 * @time: 2021/9/28 19:17
 */
public class ClassCache {
    //类全名和类类型的缓存，用于处理动态模型的sql
    public static Map<String, Class> classCaches = new HashMap<>(32);
    static {
        classCaches.put(String.class.getSimpleName(), String.class);
        classCaches.put(Boolean.class.getSimpleName(), Boolean.class);
        classCaches.put(Integer.class.getSimpleName(), Integer.class);
        classCaches.put(Byte.class.getSimpleName(), Byte.class);
        classCaches.put(Short.class.getSimpleName(), Short.class);
        classCaches.put(Float.class.getSimpleName(), Float.class);
        classCaches.put(Double.class.getSimpleName(), Double.class);
        classCaches.put(Class.class.getSimpleName(), Class.class);
        classCaches.put(Object.class.getSimpleName(), Object.class);
        classCaches.put(Long.class.getSimpleName(), Long.class);
        classCaches.put(BigDecimal.class.getSimpleName(), BigDecimal.class);
        classCaches.put(Date.class.getSimpleName(), Date.class);
        classCaches.put(Instant.class.getSimpleName(), Instant.class);
        classCaches.put(LocalDateTime.class.getSimpleName(), LocalDateTime.class);
        classCaches.put(LocalDate.class.getSimpleName(), LocalDate.class);
        classCaches.put(List.class.getSimpleName(), List.class);
        classCaches.put(ArrayList.class.getSimpleName(), ArrayList.class);
        classCaches.put(Map.class.getSimpleName(), Map.class);
        classCaches.put(HashMap.class.getSimpleName(), HashMap.class);
        classCaches.put(Set.class.getSimpleName(), Set.class);
        classCaches.put(HashSet.class.getSimpleName(), HashSet.class);
    }
}
