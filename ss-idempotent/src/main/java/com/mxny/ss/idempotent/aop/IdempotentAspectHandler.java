package com.mxny.ss.idempotent.aop;

import com.mxny.ss.idempotent.service.IdempotentTokenService;
import com.mxny.ss.redis.service.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author asiam
 */
public interface IdempotentAspectHandler {

    Object aroundIdempotent(ProceedingJoinPoint point, RedisDistributedLock redisDistributedLock) throws Throwable;

    Object aroundToken(ProceedingJoinPoint point, IdempotentTokenService idempotentTokenService) throws Throwable;
}
