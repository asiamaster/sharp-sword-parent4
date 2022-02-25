package com.mxny.ss.netty.server.cache;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端本地缓存
 * @author wangmi
 **/
public class ServerCache {
    /**
     * 保存终端号和ChannelId的关系，设备上线时赋值
     * 用于Server向终端直接下发命令(一般是心跳命令和直接回复命令)
     */
    public static Map<String, Channel> TERMINAL_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 保存ChannelId和终端号的关系，设备上线时赋值
     * 在设备第一次连接时更新，用于终端断开时，更新redis
     */
    public static final ConcurrentHashMap<ChannelId, String> CHANNELID_TERMINAL_MAP = new ConcurrentHashMap<>();

}