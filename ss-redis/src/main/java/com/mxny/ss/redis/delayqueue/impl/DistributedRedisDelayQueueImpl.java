package com.mxny.ss.redis.delayqueue.impl;

import com.alibaba.fastjson.JSON;
import com.mxny.ss.redis.delayqueue.RedisDelayQueue;
import com.mxny.ss.redis.delayqueue.consts.DelayQueueConstants;
import com.mxny.ss.redis.delayqueue.dto.DelayMessage;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 分布式延迟队列实现
 *
 * @author wm
 * @date 2021-01-26
 */
@Component
@ConditionalOnExpression("'${ss.delayqueue.distributed.enable}'=='true'")
public class DistributedRedisDelayQueueImpl<E extends DelayMessage> implements RedisDelayQueue<E> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    /**
     * 取消息，未使用
     */
    @Override
    public void poll() {
        // todo
    }

    /**
     * 发送消息
     *
     * @param e
     */
    @Override
    public void push(E e) {
        try {
            String jsonStr = JSON.toJSONString(e);
            String topic = e.getTopic();
            String zkey = DelayQueueConstants.DELAY_WAIT_KEY + topic;
            //向无序集合delay:meta:topic:wait 添加 delay:wait:+topic
            //向有序集合delay:wait:+topic 添加 具体数据， score为指定的时间点
            String script = "redis.call('sadd', KEYS[1], ARGV[1])\n" +
                            "redis.call('zadd', KEYS[2], ARGV[2], ARGV[3])\n" +
                            "return 1";

            Object[] keys = new Object[]{serialize(DelayQueueConstants.META_TOPIC_WAIT), serialize(zkey)};
            //优先使用延时时间，没有则根据延时时长计算(当前时间往后的延时秒数)
            Long score = e.getDelayTime() != null ? e.getDelayTime() : System.currentTimeMillis() + (e.getDelayDuration() * 1000);
            Object[] values = new Object[]{ serialize(zkey), serialize(String.valueOf(score)), serialize(jsonStr)};
            Long result = redisTemplate.execute((RedisCallback<Long>) connection -> {
                Object nativeConnection = connection.getNativeConnection();

                if (nativeConnection instanceof RedisAsyncCommands) {
                    RedisAsyncCommands commands = (RedisAsyncCommands) nativeConnection;
                    return (Long) commands.getStatefulConnection().sync().eval(script, ScriptOutputType.INTEGER, keys, values);
                } else if (nativeConnection instanceof RedisAdvancedClusterAsyncCommands) {
                    RedisAdvancedClusterAsyncCommands commands = (RedisAdvancedClusterAsyncCommands) nativeConnection;
                    return (Long) commands.getStatefulConnection().sync().eval(script, ScriptOutputType.INTEGER, keys, values);
                }
                return 0L;
            });
            if(result != null && result > 0) {
                logger.debug("消息推送成功进入等待队列, topic: {}", e.getTopic());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private byte[] serialize(String key) {
        RedisSerializer<String> stringRedisSerializer =
                (RedisSerializer<String>) redisTemplate.getKeySerializer();
        //lettuce连接包下序列化键值，否则无法用默认的ByteArrayCodec解析
        return stringRedisSerializer.serialize(key);
    }
}
