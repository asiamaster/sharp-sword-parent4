package com.mxny.ss.retrofitful.aop.invocation;

import java.lang.reflect.Method;

/**
 * 方法调用切点
 * 记录AOP调用上下文信息
 */
public class Invocation {

    private Method method;

    private Object[] args;

    private Object proxy;

    // 代理的类名
    private Class<?> proxyClazz;

    public Class<?> getProxyClazz() {
        return proxyClazz;
    }

    public void setProxyClazz(Class<?> proxyClazz) {
        this.proxyClazz = proxyClazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

}
