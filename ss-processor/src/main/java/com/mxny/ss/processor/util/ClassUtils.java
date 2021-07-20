package com.mxny.ss.processor.util;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public static final Map<String, Class<?>> classMap = new HashMap<>(8);
    static {
        classMap.put("int", int.class);
        classMap.put("double", double.class);
        classMap.put("long", long.class);
        classMap.put("short", short.class);
        classMap.put("byte", byte.class);
        classMap.put("boolean", boolean.class);
        classMap.put("char", char.class);
        classMap.put("float", float.class);
        classMap.put("void", void.class);
    }
    public static synchronized Class<?> forName(String className) {
        Class<?> clazz = classMap.get(className);
        if (clazz == null) {
            Class<?> aClass = null;
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                //如果该类在默认类加载器中找不到，则使用javassist动态创建
                ClassPool classPool = ClassPool.getDefault();
                CtClass newClass = classPool.makeClass(className);
                try {
                    aClass = newClass.toClass();
                } catch (CannotCompileException cannotCompileException) {
                    //dont care
                }
            }
            classMap.put(className, aClass);
            return aClass;
        }else{
            return clazz;
        }
    }

    public static Class put(String className, Class clazz){
        classMap.put(className, clazz);
        return clazz;
    }
}
