package com.mxny.ss.retrofitful;

import com.mxny.ss.retrofitful.annotation.Restful;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Proxy;

/**
 * Created by asiamastor on 2016/11/28.
 */
public class RestfulUtil {

    public static <T> T getImpl(Class<T> clazz){
        if (!clazz.isInterface()) {
            throw new RuntimeException(clazz.getName()+"不是接口");
        }
        Restful restful = clazz.getAnnotation(Restful.class);
        if(restful == null) return null;
        if(StringUtils.isBlank(restful.baseUrl()) && StringUtils.isBlank(restful.value())){
            //baseUrl或value必填
            throw new RuntimeException("@Restful注解的baseUrl或value必填");
        }
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class<?>[] { clazz }, new RestfulInterfaceHandler<T>(clazz));

    }


}
