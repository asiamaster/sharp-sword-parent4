package com.mxny.ss.uid.mapper;

import com.mxny.ss.base.MyMapper;
import com.mxny.ss.uid.domain.BizNumber;
import com.mxny.ss.uid.domain.BizNumberAndRule;

public interface BizNumberMapper extends MyMapper<BizNumber> {

    BizNumberAndRule getBizNumberAndRule(String type);
}