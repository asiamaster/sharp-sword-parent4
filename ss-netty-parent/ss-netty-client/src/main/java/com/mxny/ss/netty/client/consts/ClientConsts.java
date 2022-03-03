package com.mxny.ss.netty.client.consts;

/**
 * 客户端常量
 * @author wangmi
 **/
public class ClientConsts {
    //客户端处理器类全名key
    public static final String CLIENT_HANDLER = "ss.netty.client.handler";
    //客户端解码器key(类全名)，多个以逗号分隔，默认为com.mxny.ss.netty.server.decoder.MessageDecoder
    public static final String CLIENT_DECODERS = "ss.netty.client.decoders";
    //客户端编码器key(类全名)，多个以逗号分隔，默认为com.mxny.ss.netty.commons.channelhandler.MessageEncoder,com.mxny.ss.netty.commons.channelhandler.AcknowledgeEncoder
    public static final String CLIENT_ENCODERS = "ss.netty.client.encoders";
}