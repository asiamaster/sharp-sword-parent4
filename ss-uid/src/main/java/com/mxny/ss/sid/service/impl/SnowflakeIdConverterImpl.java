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
import com.mxny.ss.sid.service.SnowflakeIdConverter;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdConverterImpl implements SnowflakeIdConverter {
  @Override
  public long convert(SnowflakeId id) {
    long ret = 0;
    ret |= id.getSequence();
    ret |= id.getWorkerId() << SnowflakeIdMeta.SEQUENCE_BITS;
    ret |= id.getDatacenterId() << SnowflakeIdMeta.DATACENTER_ID_SHIFT_BITS;
    ret |= id.getTimeStamp() << SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS;
    return ret;
  }

  @Override
  public SnowflakeId convert(long id) {
    SnowflakeId ret = new SnowflakeId();
    ret.setSequence(id & SnowflakeIdMeta.SEQUENCE_MASK);
    ret.setWorkerId((id >>> SnowflakeIdMeta.SEQUENCE_BITS) & SnowflakeIdMeta.WORKER_ID_MASK);
    ret.setDatacenterId((id >>> SnowflakeIdMeta.DATACENTER_ID_SHIFT_BITS) & SnowflakeIdMeta.WORKER_ID_MASK);
    ret.setTimeStamp((id >>> SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS) & SnowflakeIdMeta.TIMESTAMP_MASK);
    return ret;
  }
}
