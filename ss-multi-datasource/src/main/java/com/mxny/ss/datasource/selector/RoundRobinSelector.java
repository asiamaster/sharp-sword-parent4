package com.mxny.ss.datasource.selector;

import com.mxny.ss.datasource.DataSourceManager;
import com.mxny.ss.datasource.strategy.RoundRobinStrategy;

/**
 * 轮循选择器
 * @author asiamastor
 *
 */
public class RoundRobinSelector extends OneMasterMultiSlavesDataSourceSelector {

	private RoundRobinStrategy strategy;

	public RoundRobinSelector() {
		strategy = new RoundRobinStrategy(DataSourceManager.slaves.size());
	}

	@Override
	protected String fetchSlave() {
		return DataSourceManager.slaves.get(strategy.next());
	}

}
