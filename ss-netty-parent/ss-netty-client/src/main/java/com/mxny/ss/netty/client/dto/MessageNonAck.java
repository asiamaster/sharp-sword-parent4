package com.mxny.ss.netty.client.dto;

/**
 * @author: WangMi
 * @time: 2022/2/25 12:45
 */

import com.mxny.ss.netty.commons.Message;
import io.netty.channel.Channel;

/**
 * 客户端未收到服务端回复Acknowledge时的重试发送对象
 */
public class MessageNonAck {
    private final long id;
    private final Message msg;
    private final Channel channel;
    private final long timestamp = System.currentTimeMillis();

    public MessageNonAck(Message msg, Channel channel) {
        this.msg = msg;
        this.channel = channel;
        id = msg.sequence();
    }

    public long getId() {
        return id;
    }

    public Message getMsg() {
        return msg;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getTimestamp() {
        return timestamp;
    }
}