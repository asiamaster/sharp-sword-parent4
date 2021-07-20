package com.mxny.ss.datasource.domain;

/**
 * Hikari连接池配置
 */
public class HikariDataSourceDto extends BaseDataSourceDto {
    // 最小空闲连接，默认值10
    private Integer minimumIdle = 10;
    // 最大连接数，官方默认值10
    private Integer maximumPoolSize = 20;
    // 池中连接最长生命周期, 如果不等于0且小于30秒则会被重置回30分钟
    private Integer maxLifetime = 1800000;
    // 连接允许在池中闲置的最长时间, 如果idleTimeout+1秒>maxLifetime 且 maxLifetime>0，则会被重置为0（代表永远不会退出）；如果idleTimeout!=0且小于10秒，则会被重置为10秒
    private Integer idleTimeout = 600000;
    // 自动提交从池中返回的连接
    private Boolean autoCommit = true;
    // 连接池的用户定义名称，主要出现在日志记录和JMX管理控制台中以识别池和池配置
    private String poolName = "HikariPool-1";
    // 等待来自池的连接的最大毫秒数, 如果小于250毫秒，则被重置回30秒
    private Long connectionTimeout = 30000L;
    // 如果您的驱动程序支持JDBC4，我们强烈建议您不要设置此属性
    private String connectionTestQuery = "";
    // 是否在其自己的事务中隔离内部池查询，例如连接活动测试
    private Boolean isolateInternalQueries = false;
    // 从池中获取的连接是否默认处于只读模式
    private Boolean readOnly = false;
    // 连接将被测试活动的最大时间量, 如果小于250毫秒，则会被重置回5秒
    private Long validationTimeout = 50000L;

    public Integer getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Integer maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public Long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getConnectionTestQuery() {
        return connectionTestQuery;
    }

    public void setConnectionTestQuery(String connectionTestQuery) {
        this.connectionTestQuery = connectionTestQuery;
    }

    public Boolean getIsolateInternalQueries() {
        return isolateInternalQueries;
    }

    public void setIsolateInternalQueries(Boolean isolateInternalQueries) {
        this.isolateInternalQueries = isolateInternalQueries;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Long getValidationTimeout() {
        return validationTimeout;
    }

    public void setValidationTimeout(Long validationTimeout) {
        this.validationTimeout = validationTimeout;
    }
}
