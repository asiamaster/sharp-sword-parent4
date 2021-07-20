package com.mxny.ss.datasource.domain;

import java.util.Properties;

/**
 * Druid数据源传输对象
 * 参考DruidAbstractDataSource
 */
public class DruidDataSourceDto extends BaseDataSourceDto{
    // 数据库最大连接数, 官方默认值为8
    Integer maxActive = 20;
    // 数据库初始化连接数, 官方默认值为0
    Integer initialSize = 1;
    // 数据库连接池最大等待时间，官方默认值为-1
    Long maxWait = 6000L;
    // 官方默认值为0
    Integer minIdle = 1;
    // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
    Long timeBetweenEvictionRunsMillis = 60 * 1000L;
    // 检测查询处理
    String validationQuery = "select 1";
    // 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效
    Boolean testWhileIdle = true;
    // 申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    Boolean testOnBorrow = false;
    // 归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
    Boolean testOnReturn = false;
    // 是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭
    Boolean poolPreparedStatements = false;
    // 配置监控统计拦截的filters，去掉后监控界面sql无法统计，'wall'用于防火墙
    String filters = "stat,slf4j";
    Properties connectionProperties;
    // 配置一个连接在池中最小生存的时间，单位是毫秒
    Long minEvictableIdleTimeMillis = 1000L * 60L * 30L;
    // 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true
    Integer maxOpenPreparedStatements = -1;
    // 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。
    // 在Druid中，不会存在Oracle下PSCache占用内存过多的问题，可以把这个数值配置大一些，比如说100
    Integer maxPoolPreparedStatementPerConnectionSize = 10;

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Long maxWait) {
        this.maxWait = maxWait;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public Boolean getTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(Boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public Boolean getTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(Boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public Boolean getTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(Boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public Boolean getPoolPreparedStatements() {
        return poolPreparedStatements;
    }

    public void setPoolPreparedStatements(Boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public Properties getConnectionProperties() {
        if(connectionProperties == null){
            connectionProperties.put("druid.stat.mergeSql", "true");
            connectionProperties.put("druid.stat.slowSqlMillis", "5000");
        }
        return connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public Long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public Integer getMaxOpenPreparedStatements() {
        return maxOpenPreparedStatements;
    }

    public void setMaxOpenPreparedStatements(Integer maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public Integer getMaxPoolPreparedStatementPerConnectionSize() {
        return maxPoolPreparedStatementPerConnectionSize;
    }

    public void setMaxPoolPreparedStatementPerConnectionSize(Integer maxPoolPreparedStatementPerConnectionSize) {
        this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    }
}
