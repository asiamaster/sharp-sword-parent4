//package com.mxny.ss.netty.server.controller;
//
//import com.alibaba.fastjson.JSONObject;
//import com.mxny.netty.commons.dto.MyMsg;
//import com.mxny.ss.domain.BaseOutput;
//import com.mxny.ss.netty.commons.Message;
//import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
//import com.mxny.ss.netty.server.cache.ServerCache;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import static com.mxny.ss.netty.commons.serializer.SerializerHolder.serializerImpl;
//
///**
// * 服务端控制器
// */
//@RestController
//@RequestMapping("/server")
//public class ServerController {
//
//    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);
//
//    /**
//     * 分页查询Sensor，返回easyui分页信息
//     * http://localhost:8081/client/send?msg=1
//     * @param id
//     * @return String
//     * @throws Exception
//     */
//    @GetMapping(value = "/send")
//    public BaseOutput send(@RequestParam String id) {
//        MyMsg msgBody = new MyMsg();
//        msgBody.setId(Long.parseLong(id));
//        msgBody.setCode("code"+id);
//        Message message = new Message();
//        message.setCmd(NettyProtocolConsts.CMD_SERVER_WRITE);
//        message.setData(msgBody);
//        if (!ServerCache.TERMINAL_CHANNEL_MAP.containsKey(id)) {
//            return BaseOutput.failure("id[" + id + "]不存在");
//        }
//        //获取到channel发送双方规定的message格式的信息
//        ServerCache.TERMINAL_CHANNEL_MAP.get(id).writeAndFlush(message).addListener(new ChannelFutureListener() {
//
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                if(!future.isSuccess()) {
//                    logger.info("send fail,reason is {}", future.cause().getMessage());
//                }
//            }
//        });
//        return BaseOutput.success();
//    }
//
//    /**
//     *
//     * @param args
//     */
//    public static void main(String[] args) {
//        MyMsg msgBody = new MyMsg();
//        msgBody.setId(9L);
//        msgBody.setCode("001A");
//        String s = JSONObject.toJSONString(msgBody);
//        byte[] bytes = serializerImpl().writeObject(s);
//        String s1 = serializerImpl().readObject(bytes, String.class);
//        System.out.println(s1);
//    }
//
//}
