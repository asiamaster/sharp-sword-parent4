package com.mxny.ss.service;

import com.mxny.ss.base.BaseServiceImpl;
import com.mxny.ss.dao.mapper.DynamicFieldMapper;
import com.mxny.ss.domain.DynamicField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2021-08-17 15:33:45.
 */
@Service
public class DynamicFieldService extends BaseServiceImpl<DynamicField, Long> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DynamicFieldService.class);

    public DynamicFieldMapper getActualDao() {
        return (DynamicFieldMapper)getDao();
    }





}