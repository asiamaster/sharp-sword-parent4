package com.mxny.ss.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

/**
 * @author: WangMi
 * @time: 2022/3/29 17:33
 */
public class JsonUtils {

    /**
     * 判断JSON是否合规，成功返回parse成功的对象，避免重复反序列化。 失败返回null
     * @param log
     * @return
     */
    public static Object isValid(String log){
        try {
            return JSON.parse(log);
        } catch (JSONException e) {
            return null;
        }
    }
}
