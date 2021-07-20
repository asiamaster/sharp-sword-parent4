package com.mxny.ss.rocketmq;

import com.mxny.ss.rocketmq.exception.RocketMqException;

public interface RocketMQConsumer {
	void startListener()
			throws RocketMqException;

	void stopListener()
			throws RocketMqException;
}
