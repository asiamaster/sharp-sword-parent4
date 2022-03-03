//package com.mxny.ss.netty.server.handler;
//
//import com.mxny.ss.netty.commons.Acknowledge;
//import com.mxny.ss.netty.commons.Message;
//import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
//import com.mxny.ss.netty.server.cache.ServerCache;
//import io.netty.channel.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author: WangMi
// * @time: 2022/2/25 11:42
// */
//@ChannelHandler.Sharable
//public class ServerMessageHandler extends SimpleChannelInboundHandler<Message> {
//
//    private static final Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        super.channelInactive(ctx);
//    }
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, Message message) {
//        Channel channel = ctx.channel();
//        Object data = message.getData();
//        switch (message.getCmd()) {
//            case NettyProtocolConsts.CMD_LOGIN: {
//                String terminalId = data.toString();
//                ServerCache.TERMINAL_CHANNEL_MAP.put(terminalId, channel);
//                ServerCache.CHANNELID_TERMINAL_MAP.put(channel.id(), terminalId);
//                logger.debug("客户端登录， 终端id:" + terminalId);
//                // 接收到发布信息的时候，要给Client端回复登录完成的ACK
//                channel.writeAndFlush(new Acknowledge(message.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//                break;
//            }
//            case NettyProtocolConsts.CMD_LOGOUT: {
//                String terminalId = data.toString();
//                if (ServerCache.TERMINAL_CHANNEL_MAP.containsKey(terminalId)) {
//                    ServerCache.TERMINAL_CHANNEL_MAP.remove(terminalId);
//                }
//                if (ServerCache.CHANNELID_TERMINAL_MAP.containsKey(channel.id())) {
//                    ServerCache.CHANNELID_TERMINAL_MAP.remove(channel.id());
//                }
//                logger.debug("客户端登出， 终端id:" + terminalId);
//                // 接收到发布信息的时候，要给Client端回复登录完成的ACK
//                channel.writeAndFlush(new Acknowledge(message.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//                break;
//            }
//            case NettyProtocolConsts.CMD_TERMINAL_REQUEST_TIME: {
//                message.setSource(NettyProtocolConsts.SOURCE_SERVER);
//                message.setTransferType(NettyProtocolConsts.TRANSFER_TYPE_RESPONSE);
//                message.setData(System.currentTimeMillis());
//                channel.writeAndFlush(message).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//                break;
//            }
//            default:{
//                logger.debug("收到客户端消息:"+data.toString());
//                // 接收到发布信息的时候，要给Client端回复正常响应的ACK
//                channel.writeAndFlush(new Acknowledge(message.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
//            }
//        }
////        //终端登录
////        if (message.getCmd() == NettyProtocolConsts.CMD_LOGIN) {
////            String terminalId = data.toString();
////            ServerCache.TERMINAL_CHANNEL_MAP.put(terminalId, channel);
////            ServerCache.CHANNELID_TERMINAL_MAP.put(channel.id(), terminalId);
////            logger.debug("客户端登录， 终端id:"+terminalId);
////            // 接收到发布信息的时候，要给Client端回复登录完成的ACK
////            channel.writeAndFlush(new Acknowledge(message.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
////        }else if(message.getCmd() == NettyProtocolConsts.CMD_LOGOUT) {
////            String terminalId = data.toString();
////            if(ServerCache.TERMINAL_CHANNEL_MAP.containsKey(terminalId)){
////                ServerCache.TERMINAL_CHANNEL_MAP.remove(terminalId);
////            }
////            if(ServerCache.CHANNELID_TERMINAL_MAP.containsKey(channel.id())){
////                ServerCache.CHANNELID_TERMINAL_MAP.remove(channel.id());
////            }
////            logger.debug("客户端登出， 终端id:"+terminalId);
////            // 接收到发布信息的时候，要给Client端回复登录完成的ACK
////            channel.writeAndFlush(new Acknowledge(message.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
////        }else {
////            logger.debug("收到客户端消息:"+data.toString());
////            // 接收到发布信息的时候，要给Client端回复正常响应的ACK
////            channel.writeAndFlush(new Acknowledge(message.getSequence())).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
////        }
//    }
//}
