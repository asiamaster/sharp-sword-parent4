//package com.mxny.ss.netty.client.handler;
//
//import com.mxny.ss.netty.client.cache.ClientCache;
//import com.mxny.ss.netty.commons.Acknowledge;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author: WangMi
// * @time: 2022/2/25 11:55
// */
//@ChannelHandler.Sharable
//public class ClientMessageHandler extends ChannelInboundHandlerAdapter {
//    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) {
//        if(msg instanceof Acknowledge){
//            logger.debug("收到server端的Ack信息，无需再次发送信息, sequence:{}", ((Acknowledge)msg).sequence());
//            ClientCache.messagesNonAcks.remove(((Acknowledge)msg).sequence());
//        }else{
//            logger.debug("客户端收到消息:"+ msg);
//        }
//    }
//}
