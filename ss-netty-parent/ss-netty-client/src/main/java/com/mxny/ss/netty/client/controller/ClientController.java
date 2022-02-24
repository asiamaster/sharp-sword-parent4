//package com.mxny.ss.netty.client.controller;
//
//import com.mxny.ss.netty.client.cache.ClientCache;
//import com.mxny.ss.netty.client.connector.DefaultCommonClientConnector;
//import com.mxny.ss.netty.commons.Message;
//import com.mxny.ss.netty.commons.dto.MsgBody;
//import com.mxny.ss.domain.BaseOutput;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.*;
//
//import static com.mxny.ss.netty.commons.NettyCommonProtocol.REQUEST;
//
///**
// * 客户端控制器
// */
//@RestController
//@RequestMapping("/client")
//public class ClientController {
//
//    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
//
//    /**
//     * 分页查询Sensor，返回easyui分页信息
//     * http://localhost:8081/client/send?msg=1
//     * @param msg
//     * @return String
//     * @throws Exception
//     */
//    @GetMapping(value = "/send")
//    public BaseOutput send(@RequestParam String msg) {
//        MsgBody msgBody = new MsgBody();
//        msgBody.setId(9L);
//        msgBody.setName("测试消息");
//        Message message = new Message();
//        message.sign(REQUEST);
//        message.data(msgBody);
//        //获取到channel发送双方规定的message格式的信息
//        ClientCache.channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
//
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                if(!future.isSuccess()) {
//                    logger.info("send fail,reason is {}", future.cause().getMessage());
//                }
//            }
//        });
//        //防止对象处理发生异常的情况
//        DefaultCommonClientConnector.MessageNonAck msgNonAck = new DefaultCommonClientConnector.MessageNonAck(message, ClientCache.channel);
//        ClientCache.clientConnector.addNeedAckMessageInfo(msgNonAck);
//        return BaseOutput.success();
//    }
//
//
//
//}
