package com.mxny.ss.retrofitful.aop.service;

import com.mxny.ss.retrofitful.aop.invocation.Invocation;

/**
 * ezrestful接口服务
 */
public interface RestfulService {


    Object invoke(Invocation invocation);

}
