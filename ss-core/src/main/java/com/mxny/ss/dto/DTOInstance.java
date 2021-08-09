package com.mxny.ss.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO实例缓存
 */
public class DTOInstance {
    public static final Map<Class<? extends IDTO>, Class<? extends IDTO>> CLASS_CACHE = new HashMap<>(32);
    public static final Map<Class<? extends IDTO>, IDTO> INSTANCE_CACHE = new HashMap<>(32);
    public static final String SUFFIX = "$Impl";
    //根据DTOScan注解是否扫描到了IDTO接口，判断在框架中是否主力使用DTOInstance
    public static Boolean useInstance = false;
}
