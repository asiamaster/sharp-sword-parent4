package com.mxny.ss.netty.commons;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerHolder {

    ChannelHandler[] handlers();
}
