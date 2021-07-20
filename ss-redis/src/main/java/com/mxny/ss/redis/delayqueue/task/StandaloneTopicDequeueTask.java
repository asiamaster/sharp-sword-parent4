package com.mxny.ss.redis.delayqueue.task;

import com.alibaba.fastjson.JSON;
import com.mxny.ss.component.CustomThreadPoolExecutorCache;
import com.mxny.ss.redis.delayqueue.annotation.StreamListener;
import com.mxny.ss.redis.delayqueue.component.BeanMethodCacheComponent;
import com.mxny.ss.redis.delayqueue.consts.DelayQueueConstants;
import com.mxny.ss.redis.delayqueue.dto.DelayMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * 单实例版消息出列处理器
 * @author asiamaster
 * date 2021-01-27
 */
@Component
@ConditionalOnExpression("'${ss.delayqueue.standalone.enable}'=='true'")
public class StandaloneTopicDequeueTask {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private BeanMethodCacheComponent beanMethodCacheComponent;
    @Resource
    private CustomThreadPoolExecutorCache customThreadPoolExecutorCache;

    /**
     * 每秒执行一次
     */
    @Scheduled(cron = "${ss.standaloneTopicDequeueTask.scheduled:0/1 * * * * ?}")
    public void scheduledTask() {
        try {
            //获取所有的topic，再根据topic查询已到期的
            Set<String> topics = redisTemplate.opsForSet().members(DelayQueueConstants.META_TOPIC);
            Map<Object, Method> beanMethod = beanMethodCacheComponent.getBeanMethod(StreamListener.class);
            for (String topic : topics) {
                if (!redisTemplate.hasKey(topic)) {
                    // 如果 KEY 不存在元数据中删除
                    redisTemplate.opsForSet().remove(DelayQueueConstants.META_TOPIC, topic);
                    continue;
                }
                Long startTime = System.currentTimeMillis();

                Set<String> sets = redisTemplate.opsForZSet().rangeByScore(topic, 0, startTime);
                if(sets.isEmpty()){
                    continue;
                }
                try {
                    Iterator<String> iterator = sets.iterator();
                    String delayMessageJson = null;
                    while (iterator.hasNext()) {
                        delayMessageJson = iterator.next();
                        for (Map.Entry<Object, Method> entry : beanMethod.entrySet()) {
                            DelayMessage message = JSON.parseObject(delayMessageJson, DelayMessage.class);
                            StreamListener streamListener = entry.getValue().getAnnotation(StreamListener.class);
                            if (!streamListener.value().equals(message.getTopic())) {
                                continue;
                            }
                            String finalDelayMessageJson = delayMessageJson;
                            customThreadPoolExecutorCache.getExecutor(DelayQueueConstants.DELAY_QUEUE_EXECUTOR_KEY).submit(() -> {
                                try {
                                    logger.debug("消息到期发送到消息监听器, topic: {}", message.getTopic());
                                    entry.getValue().invoke(entry.getKey(), message);
                                } catch (Throwable t) {
                                    // 失败重新放入失败队列
                                    String failKey = topic.replace(DelayQueueConstants.DELAY_QUEUE_KEY, DelayQueueConstants.DELAY_QUEUE_FAIL_KEY);
                                    redisTemplate.opsForList().rightPush(failKey, finalDelayMessageJson);
                                    logger.warn("延时队列任务处理异常: ", t);
                                }
                            });
                            //当前消息被处理后就退出，不能再让其它的StreamListener处理
                            break;
                        }
                    }
                }finally {
                    redisTemplate.opsForZSet().removeRangeByScore(topic, 0, startTime);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
