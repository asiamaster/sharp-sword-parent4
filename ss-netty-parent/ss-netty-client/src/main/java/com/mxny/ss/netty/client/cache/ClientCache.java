package com.mxny.ss.netty.client.cache;

import com.mxny.ss.netty.client.connector.DefaultCommonClientConnector;
import io.netty.channel.Channel;

/**
 * 客户端本地缓存
 * @author wangmi
 **/
public class ClientCache {
    //和服务端的连接通道
    public static Channel channel;
    //连接器
    public static DefaultCommonClientConnector clientConnector;

}