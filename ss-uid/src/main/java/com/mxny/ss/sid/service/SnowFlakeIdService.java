package com.mxny.ss.sid.service;

import com.mxny.ss.sid.dto.SnowflakeId;

import java.util.Date;

public interface SnowFlakeIdService {
  /**
   * 生成唯一id
   * @return
   */
  long nextId();

  /**
   * 反解析id为SnowflakeId对象
   * @param id
   * @return
   */
  SnowflakeId expId(long id);

  /**
   * 对时间戳单独进行解析
   *
   * @param time 时间戳
   * @return 生成的Date时间
   */
  Date transTime(long time);

  /**
   * 根据时间戳和序列号生成ID
   *
   * @param timeStamp 时间戳
   * @param sequence 序列号
   * @return 生成的ID
   */
  long makeId(long timeStamp, long sequence);

  /**
   * 根据时间戳、机器ID和序列号生成ID
   *
   * @param timeStamp 时间戳
   * @param worker 机器ID
   * @param sequence 序列号
   * @return 生成的ID
   */
  long makeId(long timeStamp, long datacenter, long worker, long sequence);
}
