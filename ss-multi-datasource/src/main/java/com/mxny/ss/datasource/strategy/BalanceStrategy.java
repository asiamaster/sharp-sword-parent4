package com.mxny.ss.datasource.strategy;

/**
 * balance strategy, fetch index from the list
 * 
 * @author asiamastor
 *
 */
public abstract class BalanceStrategy {

	/**
	 * fetch next index
	 * 
	 * @return
	 */
	public abstract int next();
}
