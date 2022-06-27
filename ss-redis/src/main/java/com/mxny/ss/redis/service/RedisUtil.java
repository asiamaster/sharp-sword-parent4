package com.mxny.ss.redis.service;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * redis 工具类
 *
 */
@SuppressWarnings("unchecked")
@Component
@ConditionalOnExpression("'${redis.enable}'=='true'")
public class RedisUtil {
    @SuppressWarnings("rawtypes")
    @Resource(name="redisTemplate")
    protected RedisTemplate redisTemplate;

    public static final String UNLOCK_LUA;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    /**
     * 集合、列表、Set等对象自行取redisTemplate操作
     * @return
     */
    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    /**
     * 批量删除对应的value
     *
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            remove(key);
        }
    }
    /**
     * 批量删除key
     *
     * @param pattern
     */
    public void removePattern(final String pattern) {
        Set<Serializable> keys = redisTemplate.keys(pattern);
        if (keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }
    /**
     * 删除对应的value
     *
     * @param key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }
    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public Boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }
    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        return result;
    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
    public Long lPush(String k, Object v) {
        return redisTemplate.opsForList().rightPush(k, v);
    }

    /**
     * 列表获取
     *
     * @param k
     * @param start
     * @param end
     * @return
     */
    public List<Object> lRange(String k, long start, long end) {
        return redisTemplate.opsForList().range(k, start, end);
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public <T> T get(final String key, Class<T> clazz) {
        Object result = null;
        ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        if(null != result && result.getClass().isAssignableFrom(clazz)){
            return (T)result;
        }
        return result == null ? null : JSON.parseObject(result.toString(), clazz);
    }

    /**
     * 根据key自增value
     * @param key
     * @param value
     * @return
     */
    public Long increment(String key, Long value){
        return redisTemplate.opsForValue().increment(key, value);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public void set(final String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */
    public Long add(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
    public Set<Object> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */
    public Boolean zAdd(String key, Object value, double scoure) {
        return redisTemplate.opsForZSet().add(key, value, scoure);
    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<Object> rangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 过期时间，单位秒
     * @return
     */
    public void set(final String key, Object value, Long expireTime) {
        set(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit 过期时间单位枚举
     * @return
     */
    public void set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return 是否获取成功
     */
    public Boolean setIfAbsent(final String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime 过期时间，单位秒
     * @return 是否获取成功
     */
    public boolean setIfAbsent(final String key, Object value, Long expireTime) {
        return setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
    }

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit 过期时间单位枚举
     * @return 是否获取成功
     */
    public Boolean setIfAbsent(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, timeUnit);
    }

    /**
     * 推后过期时间
     * @param key
     * @param timeout
     * @param timeUnit
     * @return
     */
    public Boolean expire(String key, long timeout, TimeUnit timeUnit){
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 推后过期时间到指定日期
     * @param key
     * @param date
     * @return
     */
    public Boolean expireAt(String key, Date date){
        return redisTemplate.expireAt(key, date);
    }

    /**
     * 获取锁，未获取或异常到返回false
     * key作为获取锁的条件，key和value将作为释放锁的条件
     * @param key
     * @param value
     * @param expire 单位秒
     * @return  获取失败返回false
     */
    public boolean tryGetLock(String key, String value,  long expire) {
        try {
            RedisCallback<Boolean> callback = (connection) -> {
                return connection.set(key.getBytes(Charset.forName("UTF-8")), value.getBytes(Charset.forName("UTF-8")), Expiration.seconds(expire), RedisStringCommands.SetOption.SET_IF_ABSENT);
            };
            return (Boolean)redisTemplate.execute(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 同步获取锁，异常时返回false
     * key作为获取锁的条件，key和value将作为释放锁的条件
     * 锁无限等待，每次获取失败后睡眠10毫秒
     * @param key
     * @param value
     * @param expire 单位秒
     * @return  获取失败返回false
     */
    public boolean tryGetLockSync(String key, String value, long expire) {
        return tryGetLockSync(key, value, expire, Long.MAX_VALUE, TimeUnit.SECONDS, Long.MAX_VALUE, 10L, TimeUnit.MILLISECONDS);
    }



    /**
     * 同步获取锁，异常时返回false
     * key作为获取锁的条件，key和value将作为释放锁的条件
     * @param key
     * @param value
     * @param expire 锁过期时间, 单位秒
     * @param awaitTime  锁等待超时时间
     * @param awaitUnit  锁等待超时时间单位
     * @param retryCount 最大获取次数
     * @param sleepTime  每次获取失败后的睡眠时间
     * @param sleepUnit  每次获取失败后的睡眠时间单位
     * @return  获取失败返回false
     */
    public boolean tryGetLockSync(String key, String value, long expire, long awaitTime, TimeUnit awaitUnit, long retryCount, long sleepTime, TimeUnit sleepUnit) {
        if(tryGetLock(key, value, expire)){
            return true;
        }
        //不行再获取
        long nanos = awaitUnit.toNanos(awaitTime);
        final long deadline = System.nanoTime() + nanos;
        int count = 0;
        while (true) {
            nanos = deadline - System.nanoTime();
            //超时
            if (nanos <= 0L) {
                return false;
            }
            if (tryGetLock(key, value, expire)) {
                return true;
            }
            //如果大于最大获取次数或者线程被中断
            if (count++ > retryCount || Thread.interrupted()) {
                return false;
            }
            //阻塞
            LockSupport.parkNanos(sleepUnit.toNanos(sleepTime));
        }
    }


    /**
     * Lettuce方式释放锁
     * 根据lockKey从redis获取到的值和lockValue对比，如果相同则根据key删除缓存并返回true，不同则返回false
     * @param lockKey
     * @param lockValue
     * @return
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        RedisCallback<Boolean> callback = (connection) -> {
            return connection.eval(UNLOCK_LUA.getBytes(), ReturnType.BOOLEAN ,1, lockKey.getBytes(Charset.forName("UTF-8")), lockValue.getBytes(Charset.forName("UTF-8")));
        };
        return (Boolean)redisTemplate.execute(callback);
    }
}
