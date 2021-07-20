package com.mxny.ss.quartz.listener;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.mxny.ss.exception.ParamErrorException;
import com.mxny.ss.quartz.base.RecoveryCallback;
import com.mxny.ss.quartz.domain.ScheduleJob;
import com.mxny.ss.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;

/**
 * 调度器重试监听
 * @param
 */
public class SchedulerRetryListener implements RetryListener {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private ScheduleJob scheduleJob;
    public SchedulerRetryListener(ScheduleJob scheduleJob){
        this.scheduleJob = scheduleJob;
    }
    @Override
    public <T> void onRetry(Attempt<T> attempt) {
        // 第几次重试,(第一次重试其实是第一次调用)
       if(attempt.getAttemptNumber() == 1){
           return;
       }
        // 是什么原因导致异常
        if (attempt.hasException()) {
            log.warn("第"+attempt.getAttemptNumber()+"次调度异常:" + attempt.getExceptionCause().toString());
        }
       //最后一次重试调用兜底回调
       if(attempt.getAttemptNumber() >= scheduleJob.getRetryCount() + 1 && StringUtils.isNotBlank(scheduleJob.getRecoveryCallback())){
           try {
               RecoveryCallback recoveryCallback = getObj(scheduleJob.getRecoveryCallback(), RecoveryCallback.class);
               recoveryCallback.recover(attempt, scheduleJob);
           } catch (Exception e) {
               log.error(ExceptionUtils.getStackTrace(e));
           }

       }
        // 距离第一次重试的延迟
//        System.out.print(",delay=" + attempt.getDelaySinceFirstAttempt());
        // 重试结果: 是异常终止, 还是正常返回
//        System.out.print(",hasException=" + attempt.hasException());
//        System.out.print(",hasResult=" + attempt.hasResult());
//        else {
//            // 正常返回时的结果
//            log.info(",result=" + attempt.getMessage());
//        }
    }

    /**
     * 根据名称取对象
     * 反射或取spring bean
     * @param objName
     * @return
     */
    private <T> T getObj(String objName, Class<T> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException, BeansException, ParamErrorException {
        if(objName.contains(".")){
            Class objClass = Class.forName(objName);
            if(clazz.isAssignableFrom(objClass)){
                return (T) objClass.newInstance();
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }else{
            //这里可能bean不存在，会抛异常
            T bean = SpringUtil.getBean(objName, clazz);
            if(clazz.isAssignableFrom(bean.getClass())){
                return bean;
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }
    }

}
