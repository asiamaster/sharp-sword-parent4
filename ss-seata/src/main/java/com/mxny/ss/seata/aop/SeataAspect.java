package com.mxny.ss.seata.aop;

import com.mxny.ss.retrofitful.annotation.ReqHeader;
import com.mxny.ss.seata.consts.SeataConsts;
import io.seata.core.context.RootContext;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Seata切面
 */
//@Component
//@Aspect
//@Order(100)
//@ConditionalOnExpression("'${seata.enable}'=='true'")
public class SeataAspect {

    private ExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(SeataAspect.class);
    @PostConstruct
    public void init() {
        System.out.println("SeataAspect.init");
    }

    /**
     * 设置XID
     * @param point
     * @return
     * @throws Throwable
     */
    @Around( "@annotation(com.mxny.ss.seata.annotation.GlobalTx)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        //初始化LogContext
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Annotation[][] ass = method.getParameterAnnotations();
        if(ass == null || ass.length == 0){
            return point.proceed();
        }
        retry:
        for(int i=0; i<ass.length; i++) {
            for (int j = 0; j < ass[i].length; j++) {
                Annotation annotation = ass[i][j];
                if (ReqHeader.class.equals(annotation.annotationType())) {
                    Map<String, String> headerMap = (Map)point.getArgs()[i];
                    if(headerMap == null) {
                        headerMap = new HashMap<>(1);
                    }
                    String xid = RootContext.getXID();
                    if (!StringUtils.isEmpty(xid)) {
                        headerMap.put(SeataConsts.XID, xid);
                    }
                    break retry;
                }
            }
        }
        return point.proceed();
    }
}
