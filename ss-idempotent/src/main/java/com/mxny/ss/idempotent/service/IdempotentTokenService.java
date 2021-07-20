package com.mxny.ss.idempotent.service;


import com.mxny.ss.idempotent.dto.TokenPair;

public interface IdempotentTokenService {

    /**
     * 获取token
     * @param url
     * @return key: url + tokenValue, value: tokenValue
     */
    TokenPair getToken(String url);


}
