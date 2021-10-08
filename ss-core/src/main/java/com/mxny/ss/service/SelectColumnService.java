package com.mxny.ss.service;

import com.mxny.ss.base.BaseServiceImpl;
import com.mxny.ss.dao.mapper.SelectColumnMapper;
import com.mxny.ss.domain.SelectColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 查询列服务
 * This file was generated on 2021-08-17 15:33:45.
 */
@Service
public class SelectColumnService extends BaseServiceImpl<SelectColumn, Long> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SelectColumnService.class);

    public SelectColumnMapper getActualDao() {
        return (SelectColumnMapper)getDao();
    }





}