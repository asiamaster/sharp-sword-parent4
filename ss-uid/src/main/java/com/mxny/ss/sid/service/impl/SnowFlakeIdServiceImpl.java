//                            _ooOoo_
//                           o8888888o
//                           88" . "88
//                           (| -_- |)
//                            O\ = /O
//                        ____/`---'\____
//                      .   ' \\| |// `.
//                       / \\||| : |||// \
//                     / _||||| -:- |||||- \
//                       | | \\\ - /// | |
//                     | \_| ''\---/'' | |
//                      \ .-\__ `-` ___/-. /
//                   ___`. .' /--.--\ `. . __
//                ."" '< `.___\_<|>_/___.' >'"".
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |
//                 \ \ `-. \_ __\ /__ _/ .-` / /
//         ======`-.____`-.___\_____/___.-`____.-'======
//                            `=---='
//
//         .............................................
//                  佛祖镇楼                  BUG辟易
//          佛曰:
//                  写字楼里写字间，写字间里程序员；
//                  程序人员写程序，又拿程序换酒钱。
//                  酒醒只在网上坐，酒醉还来网下眠；
//                  酒醉酒醒日复日，网上网下年复年。
//                  但愿老死电脑间，不愿鞠躬老板前；
//                  奔驰宝马贵者趣，公交自行程序员。
//                  别人笑我忒疯癫，我笑自己命太贱；
//                  不见满街漂亮妹，哪个归得程序员？
package com.mxny.ss.sid.service.impl;


import com.mxny.ss.sid.consts.SnowflakeIdMeta;
import com.mxny.ss.sid.dto.SnowflakeId;
import com.mxny.ss.sid.service.SnowFlakeIdService;
import com.mxny.ss.sid.service.SnowflakeIdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 雪花算法服务
 */
public class SnowFlakeIdServiceImpl implements SnowFlakeIdService {
    // 上一毫秒数
    private static long lastTimestamp = -1L;
    // 毫秒内Sequence(0~4095)
    private static long sequence = 0L;
    // 机器ID
    private final long workerId;
    // 中心ID
    private final long datacenterId;

    protected static final Logger log = LoggerFactory.getLogger(SnowFlakeIdServiceImpl.class);

    private SnowflakeIdConverter snowflakeIdConverter;
    /**
     * 构造
     *
     * @param datacenterId 中心ID
     * @param workerId     机器ID
     */
    public SnowFlakeIdServiceImpl(long datacenterId, long workerId, SnowflakeIdConverter snowflakeIdConverter) {
        if (workerId > SnowflakeIdMeta.MAX_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", SnowflakeIdMeta.MAX_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.snowflakeIdConverter = snowflakeIdConverter;
        log.info("worker starting. timestamp left shift {},  datacenterId id bits {}, worker id bits {}, sequence bits {}, datacenterId {}, workerid {}", SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS, SnowflakeIdMeta.DATACENTER_ID_BITS, SnowflakeIdMeta.WORKER_ID_BITS, SnowflakeIdMeta.SEQUENCE_BITS, datacenterId, workerId);
    }

    /**
     * 生成ID（线程安全）
     *
     * @return id
     */
    @Override
    public synchronized long nextId() {
        long timestamp = timeGen();

        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟被修改过，回退在上一次ID生成时间之前应当抛出异常！！！
        validateTimestamp(timestamp, lastTimestamp);

        // 如果是同一时间生成的，则进行毫秒内sequence生成
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SnowflakeIdMeta.SEQUENCE_MASK;
            // 溢出处理
            if (sequence == 0) { // 阻塞到下一毫秒,获得新时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else { // 时间戳改变，毫秒内sequence重置
            sequence = 0L;
        }
        // 上次生成ID时间截
        lastTimestamp = timestamp;

        // 移位并通过或运算组成64位ID
        return ((timestamp - SnowflakeIdMeta.START_TIME) << SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS) | (datacenterId << SnowflakeIdMeta.DATACENTER_ID_SHIFT_BITS) | (workerId << SnowflakeIdMeta.ID_SHIFT_BITS) | sequence;
    }

    /**
     * 对id进行解析
     *
     * @param id 生成的ID
     * @return 封装的ID类
     */
    @Override
    public SnowflakeId expId(long id) {
        return snowflakeIdConverter.convert(id);
    }

    /**
     * 对时间戳单独进行解析
     *
     * @param time 时间戳
     * @return 生成的Date时间
     */
    @Override
    public Date transTime(long time) {
        return new Date(time + SnowflakeIdMeta.START_TIME);
    }

    /**
     * 根据时间戳和序列号生成ID
     *
     * @param timeStamp 时间戳
     * @param sequence  序列号
     * @return 生成的ID
     */
    @Override
    public long makeId(long timeStamp, long sequence) {
        return makeId(timeStamp, datacenterId, workerId, sequence);
    }

    /**
     * 根据时间戳、机器ID和序列号生成ID
     *
     * @param timeStamp 时间戳
     * @param worker    机器ID
     * @param sequence  序列号
     * @return 生成的ID
     */
    @Override
    public long makeId(long timeStamp, long datacenter, long worker, long sequence) {
        return snowflakeIdConverter.convert(new SnowflakeId(timeStamp, datacenter, worker, sequence));
    }


    /**
     * 如果当前时间戳小于上一次ID生成的时间戳，说明系统时钟被修改过，回退在上一次ID生成时间之前应当抛出异常！！！
     *
     * @param lastTimestamp 上一次ID生成的时间戳
     * @param timestamp     当前时间戳
     */
    private void validateTimestamp(long timestamp, long lastTimestamp) {
        if (timestamp < lastTimestamp) {
            log.error(String.format("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp));
            throw new IllegalStateException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
    }

    /**
     * 阻塞到下一毫秒,获得新时间戳
     *
     * @param lastTimestamp 上次生成ID时间截
     * @return 当前时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timeGen() {
        return System.currentTimeMillis();
    }

}
