package com.mxny.ss.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.mxny.ss.dao.mapper.CommonMapper;
import com.mxny.ss.dto.IDTO;
import com.mxny.ss.metadata.ValuePair;
import com.mxny.ss.service.CommonService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by asiamaster on 2017/7/25 0025.
 */
@Service
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public class CommonServiceImpl implements CommonService {

	@Autowired
	private CommonMapper commonMapper;

	@Override
	public List<ValuePair<?>> selectValuePair(String sql) {
		return commonMapper.selectValuePair(sql);
	}

	@Override
	public List<JSONObject> selectJSONObject(String sql, Integer page, Integer rows) {
		//为了线程安全,请勿改动下面两行代码的顺序
		PageHelper.startPage(page, rows);
		return commonMapper.selectJSONObject(sql);
	}

	@Override
	public List<Map> selectMap(String sql, Integer page, Integer rows) {
		//为了线程安全,请勿改动下面两行代码的顺序
		PageHelper.startPage(page, rows);
		return commonMapper.selectMap(sql);
	}

	@Override
	public <T extends IDTO> List<T> selectDto(String sql, Class<T> resultType, Integer page, Integer rows) {
		//为了线程安全,请勿改动下面两行代码的顺序
		PageHelper.startPage(page, rows);
		return commonMapper.selectDto(sql, resultType);
	}

	@Override
	public <T extends IDTO> List<T> selectDto(String sql, Class<T> resultType) {
		return commonMapper.selectDto(sql, resultType);
	}

	@Override
	public void execute(String sql) {
		commonMapper.execute(sql);
	}

}
