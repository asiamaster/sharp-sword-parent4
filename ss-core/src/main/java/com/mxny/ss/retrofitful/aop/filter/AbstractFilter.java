package com.mxny.ss.retrofitful.aop.filter;

import com.mxny.ss.retrofitful.aop.invocation.Invocation;

/**
 * restful拦截器抽象类
 * 所有restful拦截器实现都应该继承此类
 */
public abstract class AbstractFilter implements Filter {
    protected Filter restfulFilter;

    @Override
    public Filter getRestfulFilter() {
        return restfulFilter;
    }

    @Override
    public void setRestfulFilter(Filter restfulFilter) {
        this.restfulFilter = restfulFilter;
    }

    @Override
    public Object invoke(Invocation invocation) throws Exception{
        return getRestfulFilter().invoke(invocation);
    }


}
