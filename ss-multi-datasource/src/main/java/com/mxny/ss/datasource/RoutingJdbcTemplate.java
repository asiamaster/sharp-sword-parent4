package com.mxny.ss.datasource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 解决事物方法里不能动态切换数据源
 * 在一个事物方法里面，在获取connection的时候，会先将connection和dataSource绑定存在ThreadLocal变量中，在下次切换数据源获取新connection时，首先会从ThreadLocal变量中查看该数据源是否已经有在使用的connection，如果有的话，则将该connection直接返回。
 * 而在绑定数据源的时候传入的dataSouce并不是具体的数据源，而是整个AbstractRoutingDataSource；所以在下一次获取新connection时，会将原来的connection返回
 *
 * @author: WangMi
 * @time: 2021/9/29 16:26
 */
@Component
public class RoutingJdbcTemplate extends JdbcTemplate {

    public RoutingJdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public DataSource getDataSource() {
        DynamicRoutingDataSource dynamicRoutingDataSource =  (DynamicRoutingDataSource) super.getDataSource();
        return dynamicRoutingDataSource.determineTargetDataSource();
    }

}
