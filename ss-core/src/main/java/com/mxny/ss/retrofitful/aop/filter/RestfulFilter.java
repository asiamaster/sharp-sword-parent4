package com.mxny.ss.retrofitful.aop.filter;

import com.mxny.ss.retrofitful.aop.annotation.Order;
import com.mxny.ss.retrofitful.aop.invocation.Invocation;
import com.mxny.ss.retrofitful.aop.service.RestfulService;
import com.mxny.ss.retrofitful.aop.service.impl.RestfulServiceImpl;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 默认的restful拦截器，最后执行
 * 按@Order从小到大排序，最后调用
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@DependsOn("initConfig")
public class RestfulFilter extends AbstractFilter {
    private RestfulService restfulService;

    @PostConstruct
    public void init(){
        restfulService = new RestfulServiceImpl();
    }

    @Override
    public Object invoke(Invocation invocation) throws Exception {
        return restfulService.invoke(invocation);
    }


}
