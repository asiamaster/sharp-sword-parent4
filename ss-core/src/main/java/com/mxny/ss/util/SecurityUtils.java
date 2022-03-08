package com.mxny.ss.util;

import com.mxny.ss.dto.IDomain;
import com.mxny.ss.dto.IMybatisForceParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 安全工具
 * @author: WangMi
 * @time: 2022/3/7 15:21
 */
public class SecurityUtils {

    private static final String sqlReg = "(?:')|(?:--)|(/\\*(?:.|[\\n\\r])*?\\*/)|"+ "(\\b(select|update|and|or|delete|insert|trancate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute)\\b)";
    private static Pattern sqlPattern = Pattern.compile(sqlReg, Pattern.CASE_INSENSITIVE);

    protected static final Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 检测SQL注入
     *
     * @param value
     * @return
     */
    public static boolean checkXss(String value) {
        if (value == null || "".equals(value)) {
            return true;
        }
        if (sqlPattern.matcher(value).find()) {
            LOGGER.error("SQL注入拦截:" + value);
            return false;
        }
        return true;
    }

    /**
     * SQL注入拦截
     * @param domain
     * @return    异常返回true
     */
    public static boolean checkDomainXss(IDomain domain){
        if (domain == null) {
            return false;
        }
        if (domain instanceof IDomain) {
            IDomain iDomain = (IDomain) domain;
            if (!SecurityUtils.checkXss(iDomain.getSort())) {
                return true;
            }
            if (!SecurityUtils.checkXss(iDomain.getOrder())) {
                return true;
            }
            if (domain instanceof IMybatisForceParams) {
                IMybatisForceParams mybatisForceParams = (IMybatisForceParams) domain;
                if (mybatisForceParams.getSelectColumns() != null) {
                    for (String selectColumn : mybatisForceParams.getSelectColumns()) {
                        if (!SecurityUtils.checkXss(selectColumn)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 采用 ESAPI library 来防止XSS攻击的字符转换
     * @param value
     * @return
     */
    public static String stripXSS(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid null characters
            value = value.replaceAll("", "");
            // Avoid anything between script tags
            Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid anything in a src="..." type of e­xpression
            scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome </script> tag
            scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome <script ...> tag
            scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid eval(...) e­xpressions
            scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid e­xpression(...) e­xpressions
            scriptPattern = Pattern.compile("e­xpression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid javascript:... e­xpressions
            scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid vbscript:... e­xpressions
            scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid onload= e­xpressions
            scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
        }
        return value;
    }

}
