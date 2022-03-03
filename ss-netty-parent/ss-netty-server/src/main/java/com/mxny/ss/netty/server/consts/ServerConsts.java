package com.mxny.ss.netty.server.consts;

/**
 * 服务端常量
 * @author wangmi
 **/
public class ServerConsts {
    //服务端处理器key(类全名)
    public static final String SERVER_HANDLER = "ss.netty.server.handler";
    //服务端解码器key(类全名)，多个以逗号分隔，默认为com.mxny.ss.netty.server.decoder.MessageDecoder
    public static final String SERVER_DECODERS = "ss.netty.server.decoders";
    //服务端编码器key(类全名)，多个以逗号分隔，默认为com.mxny.ss.netty.commons.channelhandler.MessageEncoder,com.mxny.ss.netty.commons.channelhandler.AcknowledgeEncoder
    public static final String SERVER_ENCODERS = "ss.netty.server.encoders";

}