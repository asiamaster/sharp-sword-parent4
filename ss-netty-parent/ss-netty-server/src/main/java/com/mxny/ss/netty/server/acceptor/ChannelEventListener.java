package com.mxny.ss.netty.server.acceptor;

import io.netty.channel.Channel;

/**
 * 服务端通道事件监听
 */
public interface ChannelEventListener {

    /**
     * 客户端建立连接
     * @param remoteAddr
     * @param channel
     */
    void onChannelConnect(final String remoteAddr, final Channel channel);

    /**
     * 客户端主动断开连接
     * @param remoteAddr
     * @param channel
     */
    void onChannelClose(final String remoteAddr, final Channel channel);

    /**
     * 客户端异常断开连接
     * @param remoteAddr
     * @param channel
     */
    void onChannelException(final String remoteAddr, final Channel channel);

    /**
     * 客户端Idle事件
     * @param remoteAddr
     * @param channel
     */
    void onChannelIdle(final String remoteAddr, final Channel channel);
}
