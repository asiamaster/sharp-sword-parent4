
package com.mxny.ss.seata.properties;

import com.mxny.ss.seata.boot.StarterConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Saga state machine async thread pool properties.
 *
 * @author wangmi
 */
//@Configuration("sagaAsyncThreadPoolProperties")
@ConfigurationProperties(StarterConstants.SAGA_ASYNC_THREAD_POOL_PREFIX)
public class SagaAsyncThreadPoolProperties {

    /**
     * core pool size.
     */
    private int corePoolSize = 1;

    /**
     * max pool size
     */
    private int maxPoolSize = 20;

    /**
     * keep alive time
     */
    private int keepAliveTime = 60;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
}