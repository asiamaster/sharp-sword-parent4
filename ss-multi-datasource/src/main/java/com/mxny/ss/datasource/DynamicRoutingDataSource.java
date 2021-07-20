package com.mxny.ss.datasource;

import com.mxny.ss.datasource.aop.DynamicRoutingDataSourceContextHolder;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 多数据源模式，动态路由数据源
 * Created by asiamaster on 2017/8/8 0008.
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

	private static DataSource defaultDataSource;

	private static Map<String, DataSource> dataSourceMap = new HashMap<>();

	@Override
	protected Object determineCurrentLookupKey() {
		return DynamicRoutingDataSourceContextHolder.peek();
	}

	/**
	 * 设置使用哪个数据源
	 *
	 * @return
	 */
	@Override
	public DataSource determineTargetDataSource() {
		Object key = this.determineCurrentLookupKey();
		DataSource dataSource = dataSourceMap.get(key);
		if (dataSource == null) {
			dataSource = defaultDataSource;
		}
		return dataSource;
	}

	/**
	 * 测试是否联通
	 * @param dbkey
	 * @return
	 */
	public boolean testConnection(String dbkey){
		DynamicRoutingDataSourceContextHolder.push(dbkey);
		try {
			Connection connection = determineTargetDataSource().getConnection();
			if (connection != null) {
				return true;
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		finally {
			DynamicRoutingDataSourceContextHolder.clear();
		}
		return false;
	}

	/**
	 * 检查数据源是否存在
	 * @param dsKey
	 * @return
	 */
	public static boolean checkDataSourceKey(String dsKey) {
		if (dataSourceMap.get(dsKey) != null) {
			return true;
		}
		return false;
	}

	/**
	 * 删除数据源
	 * @param dbKey
	 */
	public static void remove(String dbKey){
		dataSourceMap.remove(dbKey);
	}

	/**
	 * 获取默认数据源
	 * @return
	 */
	public static DataSource getDefaultDataSource() {
		return defaultDataSource;
	}

	/**
	 * 设置默认数据源
	 * @param defaultDataSource
	 */
	public void setDefaultDataSource(DataSource defaultDataSource) {
		DynamicRoutingDataSource.defaultDataSource = defaultDataSource;
	}

	/**
	 * 设置所有数据源
	 * @param dataSourceMap
	 */
	public static void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
		DynamicRoutingDataSource.dataSourceMap = dataSourceMap;
	}

	/**
	 * 获取所有数据源
	 * @return
	 */
	public static Map<String, DataSource> getDataSourceMap() {
		return dataSourceMap;
	}
}
