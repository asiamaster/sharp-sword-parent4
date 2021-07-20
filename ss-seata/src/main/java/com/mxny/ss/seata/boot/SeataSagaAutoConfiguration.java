/*
 *  Copyright 1999-2019 Seata.io Group.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mxny.ss.seata.boot;

import com.mxny.ss.seata.properties.SagaAsyncThreadPoolProperties;
import com.mxny.ss.seata.util.ResourceUtil;
import io.seata.saga.engine.StateMachineConfig;
import io.seata.saga.engine.StateMachineEngine;
import io.seata.saga.engine.config.DbStateMachineConfig;
import io.seata.saga.engine.impl.ProcessCtrlStateMachineEngine;
import io.seata.saga.rm.StateMachineEngineHolder;
import io.seata.spring.boot.autoconfigure.SeataAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;

import javax.sql.DataSource;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Saga auto configuration.
 *
 * @author wangmi
 */
@Configuration
@ConditionalOnProperty({StarterConstants.SEATA_PREFIX + ".enabled", StarterConstants.SAGA_PREFIX + ".enabled"})
@AutoConfigureAfter({DataSourceAutoConfiguration.class, SeataAutoConfiguration.class})
public class SeataSagaAutoConfiguration {

    public static final String SAGA_DATA_SOURCE_BEAN_NAME = "seataSagaDataSource";
    public static final String SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME = "seataSagaAsyncThreadPoolExecutor";
    public static final String SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME = "seataSagaRejectedExecutionHandler";
//    public static final String SAGA_ASYNC_THREADPOOL_PROPERTIES_BEAN_NAME = "sagaAsyncThreadPoolProperties";

    /**
     * Create state machine config bean.
     */
    @Bean
//    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean
    @ConfigurationProperties(StarterConstants.SAGA_STATE_MACHINE_PREFIX)
    public StateMachineConfig dbStateMachineConfig(
            DataSource dataSource,
            @Qualifier(SAGA_DATA_SOURCE_BEAN_NAME) @Autowired(required = false) DataSource sagaDataSource,
            @Qualifier(SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME) @Autowired(required = false) ThreadPoolExecutor threadPoolExecutor,
            @Value("${spring.application.name:}") String applicationId,
            @Value("${seata.tx-service-group:}") String txServiceGroup) {
//        DataSource dataSource = SpringUtil.getBean(DataSource.class);
        DbStateMachineConfig config = new DbStateMachineConfig();
        config.setDataSource(sagaDataSource != null ? sagaDataSource : dataSource);
        config.setApplicationId(applicationId);
        config.setTxServiceGroup(txServiceGroup);
        String[] resourceStrs = new String[]{"classpath*:/statelang/**/*.json"};
        config.setResources(ResourceUtil.getResources(resourceStrs));
        if (threadPoolExecutor != null) {
            config.setThreadPoolExecutor(threadPoolExecutor);
        }

        return config;
    }

    /**
     * Create state machine engine bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public StateMachineEngine stateMachineEngine(StateMachineConfig config) {
        ProcessCtrlStateMachineEngine engine = new ProcessCtrlStateMachineEngine();
        engine.setStateMachineConfig(config);
        //Seata Server进行事务恢复时需要通过这个Holder拿到stateMachineEngine实例
        new StateMachineEngineHolder().setStateMachineEngine(engine);
        return engine;
    }

    /**
     * The saga async thread pool executor configuration.
     */
    @Configuration
    @ConditionalOnProperty(name = StarterConstants.SAGA_STATE_MACHINE_PREFIX + ".enable-async", havingValue = "true")
    @EnableConfigurationProperties({SagaAsyncThreadPoolProperties.class})
    static class SagaAsyncThreadPoolExecutorConfiguration {

        /**
         * Create rejected execution handler bean.
         */
        @Bean(SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME)
        @ConditionalOnMissingBean
        public RejectedExecutionHandler sagaRejectedExecutionHandler() {
            return new ThreadPoolExecutor.CallerRunsPolicy();
        }

        /**
         * Create state machine async thread pool executor bean.
         */
        @Bean(SAGA_ASYNC_THREAD_POOL_EXECUTOR_BEAN_NAME)
        @ConditionalOnMissingBean
        public ThreadPoolExecutor sagaAsyncThreadPoolExecutor(
                SagaAsyncThreadPoolProperties properties,
                @Qualifier(SAGA_REJECTED_EXECUTION_HANDLER_BEAN_NAME) RejectedExecutionHandler rejectedExecutionHandler) {
            ThreadPoolExecutorFactoryBean threadFactory = new ThreadPoolExecutorFactoryBean();
            threadFactory.setBeanName("sagaStateMachineThreadPoolExecutorFactory");
            threadFactory.setThreadNamePrefix("sagaAsyncExecute-");
            threadFactory.setCorePoolSize(properties.getCorePoolSize());
            threadFactory.setMaxPoolSize(properties.getMaxPoolSize());
            threadFactory.setKeepAliveSeconds(properties.getKeepAliveTime());

            return new ThreadPoolExecutor(
                    properties.getCorePoolSize(),
                    properties.getMaxPoolSize(),
                    properties.getKeepAliveTime(),
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    threadFactory,
                    rejectedExecutionHandler
            );
        }
    }
}
