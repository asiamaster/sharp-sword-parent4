package com.mxny.ss.uid.service;

import com.mxny.ss.base.BaseService;
import com.mxny.ss.uid.domain.BizNumber;
import com.mxny.ss.uid.domain.BizNumberRule;

/**
 * 业务号服务
 */
public interface BizNumberService extends BaseService<BizNumber, Long> {

	/**
	 * 清除缓存
	 * @param type
	 */
	void clear(String type);

	/**
	 * 根据业务类型规则获取编号
	 * @param bizNumberRule 业务类型规则
	 * @return
	 */
	String getBizNumberByRule(BizNumberRule bizNumberRule);

	/**
	 * 查询一条
	 * @param bizNumber
	 * @return
	 */
	BizNumber selectOne(BizNumber bizNumber);
}