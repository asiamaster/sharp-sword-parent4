package com.mxny.ss.datasource.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.mxny.ss.datasource.DynamicRoutingDataSource;
import com.mxny.ss.datasource.aop.DynamicRoutingDataSourceContextHolder;
import com.mxny.ss.datasource.domain.DruidDataSourceDto;
import com.mxny.ss.datasource.domain.HikariDataSourceDto;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 动态(多)数据源工具
 */
public class DynamicRoutingDataSourceUtils {

    private static final Logger logger = LoggerFactory.getLogger(DynamicRoutingDataSourceUtils.class);

    /**
     * 测试是否联通
     * @param dsKey
     * @return
     */
    public static boolean testConnection(String dsKey){
        DynamicRoutingDataSourceContextHolder.push(dsKey);
        try {
            DataSource dataSource = DynamicRoutingDataSource.getDataSourceMap().get(dsKey);
            if(dataSource == null){
                return false;
            }
            Connection connection = dataSource.getConnection();
            if (connection != null) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
        if (DynamicRoutingDataSource.getDataSourceMap().get(dsKey) != null) {
            return true;
        }
        return false;
    }

    /**
     * 删除数据源
     * @param dsKey
     */
    public static void remove(String dsKey){
        DynamicRoutingDataSource.getDataSourceMap().remove(dsKey);
    }

    /**
     * 动态添加Hikari数据源
     * @param hikariDataSourceDto
     * @return 返回添加的数据源
     */
    public static DataSource addHikariDataSource(HikariDataSourceDto hikariDataSourceDto){
        // 创建基础hikari数据源
        DataSourceBuilder<HikariDataSource> hikariDataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource.class);
        hikariDataSourceBuilder.url(hikariDataSourceDto.getUrl());
        hikariDataSourceBuilder.username(hikariDataSourceDto.getUsername());
        hikariDataSourceBuilder.password(hikariDataSourceDto.getPassword());
        if(hikariDataSourceDto.getDriverClassName() != null) {
            hikariDataSourceBuilder.driverClassName(hikariDataSourceDto.getDriverClassName());
        }
        HikariDataSource hikariDataSource = hikariDataSourceBuilder.build();
        hikariDataSource.setMinimumIdle(hikariDataSourceDto.getMinimumIdle());
        hikariDataSource.setMaximumPoolSize(hikariDataSourceDto.getMaximumPoolSize());
        // 池中连接最长生命周期, 如果不等于0且小于30秒则会被重置回30分钟，单位毫秒
        hikariDataSource.setMaxLifetime(hikariDataSourceDto.getMaxLifetime());
        // 连接允许在池中闲置的最长时间, 如果idleTimeout+1秒>maxLifetime 且 maxLifetime>0，则会被重置为0（代表永远不会退出）；如果idleTimeout!=0且小于10秒，则会被重置为10秒
        hikariDataSource.setIdleTimeout(hikariDataSourceDto.getIdleTimeout());
        // 自动提交从池中返回的连接
        hikariDataSource.setAutoCommit(hikariDataSourceDto.getAutoCommit());
        // 连接池的用户定义名称，主要出现在日志记录和JMX管理控制台中以识别池和池配置
        hikariDataSource.setPoolName(hikariDataSourceDto.getPoolName());
        // 等待来自池的连接的最大毫秒数, 如果小于250毫秒，则被重置回30秒
        hikariDataSource.setConnectionTimeout(hikariDataSourceDto.getConnectionTimeout());
        // 如果您的驱动程序支持JDBC4，我们强烈建议您不要设置此属性
        hikariDataSource.setConnectionTestQuery(hikariDataSourceDto.getConnectionTestQuery());
        // 是否在其自己的事务中隔离内部池查询，例如连接活动测试
        hikariDataSource.setIsolateInternalQueries(hikariDataSourceDto.getIsolateInternalQueries());
        // 从池中获取的连接是否默认处于只读模式
        hikariDataSource.setReadOnly(hikariDataSourceDto.getReadOnly());
        //连接将被测试活动的最大时间量, 如果小于250毫秒，则会被重置回5秒
        hikariDataSource.setValidationTimeout(hikariDataSourceDto.getValidationTimeout());
        //注册到动态数据源
        DynamicRoutingDataSource.getDataSourceMap().put(hikariDataSourceDto.getDataSourceId(), hikariDataSource);
        return hikariDataSource;
    }

    /**
     * 动态添加Druid数据源
     * @param druidDataSourceDto
     * @return 返回添加的数据源
     */
    public static DataSource addDruidDataSource(DruidDataSourceDto druidDataSourceDto) throws SQLException{
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(druidDataSourceDto.getUrl());
        druidDataSource.setUsername(druidDataSourceDto.getUsername());
        druidDataSource.setPassword(druidDataSourceDto.getPassword());
        druidDataSource.setDriverClassName(druidDataSourceDto.getDriverClassName());

        druidDataSource.setInitialSize(druidDataSourceDto.getInitialSize());
        druidDataSource.setMinIdle(druidDataSourceDto.getMinIdle());
        druidDataSource.setMaxWait(druidDataSourceDto.getMaxWait());
        druidDataSource.setConnectProperties(druidDataSourceDto.getConnectionProperties());

        druidDataSource.setMaxActive(druidDataSourceDto.getMaxActive());
        druidDataSource.setTimeBetweenEvictionRunsMillis(druidDataSourceDto.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(druidDataSourceDto.getMinEvictableIdleTimeMillis());
        druidDataSource.setTestWhileIdle(druidDataSourceDto.getTestWhileIdle());
        if (StringUtils.isNotBlank(druidDataSourceDto.getValidationQuery())) {
            druidDataSource.setValidationQuery(druidDataSourceDto.getValidationQuery());
        }
        druidDataSource.setTestOnBorrow(druidDataSourceDto.getTestOnBorrow());
        druidDataSource.setTestOnReturn(druidDataSourceDto.getTestOnReturn());
        druidDataSource.setMaxOpenPreparedStatements(druidDataSourceDto.getMaxOpenPreparedStatements());
        druidDataSource.setPoolPreparedStatements(druidDataSourceDto.getPoolPreparedStatements());
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(
                druidDataSourceDto.getMaxPoolPreparedStatementPerConnectionSize());
        if (StringUtils.isNotBlank(druidDataSourceDto.getFilters())) {
            try {
                druidDataSource.setFilters(druidDataSourceDto.getFilters());
            } catch (SQLException e) {
                logger.error("初始化数据库连接池发生异常:{}", e.toString());
                throw e;
            }
        }
        //注册到动态数据源
        DynamicRoutingDataSource.getDataSourceMap().put(druidDataSourceDto.getDataSourceId(), druidDataSource);
        return druidDataSource;
    }
}
