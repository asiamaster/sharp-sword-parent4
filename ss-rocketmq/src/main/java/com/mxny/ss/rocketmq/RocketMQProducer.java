package com.mxny.ss.rocketmq;

import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.mxny.ss.rocketmq.exception.RocketMqException;

public interface RocketMQProducer
{
  void sendMsg(Message paramMessage)
    throws RocketMqException;

  void sendOrderMsg(Message paramMessage, Number paramNumber)
    throws RocketMqException;

  SendResult sendOrderMsg(Message paramMessage, String paramString)
    throws RocketMqException;

  void shutdown()
    throws RocketMqException;
}
