package com.mxny.ss.netty.client.cache;

import com.mxny.ss.netty.client.connector.DefaultCommonClientConnector;
import com.mxny.ss.netty.client.dto.MessageNonAck;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 客户端本地缓存
 * @author wangmi
 **/
public class ClientCache {
    //和服务端的连接通道
    public static Channel channel;
    //连接器
    public static DefaultCommonClientConnector clientConnector;

    public final static ConcurrentMap<Integer, MessageNonAck> messagesNonAcks = new ConcurrentHashMap<Integer, MessageNonAck>();
}