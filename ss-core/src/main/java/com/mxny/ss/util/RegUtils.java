package com.mxny.ss.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则工具类
 * @author: WangMi
 * @time: 2022/6/7 14:49
 */
public class RegUtils {

    /**
     * 替换大括号内的变量
     * 主要用于Spring MVC的PathVariable解析替换
     * @param content 如: /api/{varName}
     * @param params
     */
    public static String replaceBraceParams(String content, Map<String, String> params) {
        String pattern = "\\{(.+?)\\}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String value = params.get(key);
            m.appendReplacement(sb, value == null ? "" : value);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
