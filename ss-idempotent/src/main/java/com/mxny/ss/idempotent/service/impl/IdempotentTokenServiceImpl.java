package com.mxny.ss.idempotent.service.impl;

import com.mxny.ss.idempotent.dto.TokenPair;
import com.mxny.ss.idempotent.service.IdempotentTokenService;
import com.mxny.ss.redis.service.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentTokenServiceImpl implements IdempotentTokenService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public TokenPair getToken(String url) {
        TokenPair tokenPair = new TokenPair();
        String tokenValue = UUID.randomUUID().toString();
        tokenPair.setKey(url + tokenValue);
        tokenPair.setValue(tokenValue);
        redisUtil.set(url + tokenValue, tokenValue);
        return tokenPair;
    }

}
