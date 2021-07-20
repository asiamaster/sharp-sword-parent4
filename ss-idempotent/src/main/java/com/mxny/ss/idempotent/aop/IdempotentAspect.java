package com.mxny.ss.idempotent.aop;

import com.mxny.ss.idempotent.service.IdempotentTokenService;
import com.mxny.ss.redis.service.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 幂等切面
 */
@Component
@Aspect
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentAspect {
    @Autowired
    IdempotentTokenService idempotentTokenService;

    @Autowired
    RedisDistributedLock redisDistributedLock;

    IdempotentAspectHandler idempotentAspectHandler;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        idempotentAspectHandler = new IdempotentAspectHandlerImpl();
    }

    /**
     * 设置token
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.mxny.ss.idempotent.annotation.Token)")
    public Object token(ProceedingJoinPoint point) throws Throwable {
        return idempotentAspectHandler.aroundToken(point, idempotentTokenService);
    }

    /**
     * 幂等验证
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.mxny.ss.idempotent.annotation.Idempotent)")
    public Object idempotent(ProceedingJoinPoint point) throws Throwable {
        return idempotentAspectHandler.aroundIdempotent(point, redisDistributedLock);
    }

}
