package com.mxny.ss.quartz.base;

import com.github.rholder.retry.Attempt;
import com.mxny.ss.quartz.domain.ScheduleJob;

/**
 * 兜底回调接口
 */
public interface RecoveryCallback {
    /**
     *  兜底方法
     * @param attempt 一次执行任务
     * @param scheduleJob
     */
    void recover(Attempt attempt, ScheduleJob scheduleJob);
}
