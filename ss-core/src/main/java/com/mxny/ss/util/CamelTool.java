package com.mxny.ss.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 驼峰转换
 * 把map的key转换成驼峰命名
 *
 * @author: WM
 * @time: 2021/6/18 10:41
 */
public class CamelTool {
    public static final char UNDERLINE = '_';
    public static final char MIDLINE = '-';

    /**
     * 把map的key(中线和下线)转换成驼峰命名
     * @param map
     * @return
     */
    public static Map<String, Object> toReplaceKeyLow(Map<String, Object> map) {
        Map re_map = new HashMap();
        if (re_map != null) {
            Iterator var2 = map.entrySet().iterator();
            while (var2.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry) var2.next();
                re_map.put(underlineAndMidlineToCamel((String) entry.getKey()), map.get(entry.getKey()));
            }
            map.clear();
        }
        return re_map;
    }

    /**
     * 中线和下线转驼峰
     * @param param
     * @return
     */
    public static String underlineAndMidlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE || c == MIDLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(Character.toLowerCase(param.charAt(i)));
            }
        }
        return sb.toString();
    }

    /**
     * 下线转驼峰
     * @param param
     * @return
     */
    public static String underlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == UNDERLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(Character.toLowerCase(param.charAt(i)));
            }
        }
        return sb.toString();
    }

    /**
     * 中线转驼峰
     * @param param
     * @return
     */
    public static String midlineToCamel(String param) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (c == MIDLINE) {
                if (++i < len) {
                    sb.append(Character.toUpperCase(param.charAt(i)));
                }
            } else {
                sb.append(Character.toLowerCase(param.charAt(i)));
            }
        }
        return sb.toString();
    }

    /**
     * 驼峰转下划线
     * 首字母不转
     * @param param
     * @param toUpper
     * @return
     */
    public static String camelToUnderline(String param, boolean toUpper) {
        if (param == null || "".equals(param.trim())) {
            return "";
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c) && i != 0) {
                sb.append(UNDERLINE);
            }
            if (toUpper) {
                sb.append(Character.toUpperCase(c));  //统一都转大写
            } else {
                sb.append(Character.toLowerCase(c));  //统一都转小写
            }
        }
        return sb.toString();
    }
}
