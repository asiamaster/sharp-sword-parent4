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
package com.mxny.ss.sid.consts;

public class SnowflakeIdMeta {
    // 开始时间截 (从2019-01-01起)
    public static final long START_TIME = 1546272000000L;
    // 机器ID所占位数
    public static final long ID_BITS = 10L;
    // 机器ID最大值1023 (此移位算法可很快计算出n位二进制数所能表示的最大十进制数)
    public static final long MAX_ID = ~(-1L << ID_BITS);
    // Sequence所占位数
    public static final long SEQUENCE_BITS = 12L;
    // 机器ID偏移量12
    public static final long ID_SHIFT_BITS = SEQUENCE_BITS;
    /**
     * 机器ID向左移12位
     */
    public static final long WORKER_ID_SHIFT_BITS = SEQUENCE_BITS;
    /**
     * 机器id所占的位数
     */
    public static final long WORKER_ID_BITS = 5L;
    /**
     * 数据标识id所占的位数
     */
    public static final long DATACENTER_ID_BITS = 5L;
    /**
     * 数据标识id向左移17位(12+5)
     */
    public static final long DATACENTER_ID_SHIFT_BITS = SEQUENCE_BITS + WORKER_ID_BITS;

    // 时间戳的偏移量12+10=22
    public static final long TIMESTAMP_LEFT_SHIFT_BITS = SEQUENCE_BITS + ID_BITS;

    // WORKER和DATACENTER掩码31
    public static final long WORKER_ID_MASK = ~(-1L << WORKER_ID_BITS);

    // Sequence掩码4095
    public static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    // 机器ID掩码1023(workId+datacenterId)
    public static final long ID_MASK = ~(-1L << ID_BITS);
    // 时间戳掩码2的41次方减1
    public static final long TIMESTAMP_MASK = ~(-1L << 41L);

    /** 构造方法 */
    private SnowflakeIdMeta() {
    }
}
