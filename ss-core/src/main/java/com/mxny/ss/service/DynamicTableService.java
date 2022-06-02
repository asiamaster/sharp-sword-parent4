package com.mxny.ss.service;

import com.mxny.ss.base.BaseServiceImpl;
import com.mxny.ss.dao.mapper.DynamicTableMapper;
import com.mxny.ss.domain.DynamicTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2021-08-17 15:33:45.
 */
@Service
@ConditionalOnClass(tk.mybatis.mapper.entity.Example.class)
public class DynamicTableService extends BaseServiceImpl<DynamicTable, Long> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DynamicTableService.class);

    public DynamicTableMapper getActualDao() {
        return (DynamicTableMapper)getDao();
    }





}