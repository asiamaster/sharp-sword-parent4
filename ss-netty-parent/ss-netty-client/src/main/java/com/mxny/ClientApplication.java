//package com.mxny;
//
//import com.mxny.ss.netty.client.cache.ClientCache;
//import com.mxny.ss.netty.client.connector.DefaultCommonClientConnector;
//import com.mxny.ss.netty.commons.Message;
//import com.mxny.ss.netty.commons.dto.MsgBody;
//import com.mxny.ss.dto.DTOScan;
//import com.mxny.ss.retrofitful.annotation.RestfulScan;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelFutureListener;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
//import org.springframework.context.annotation.ComponentScan;
//
//import static com.mxny.ss.netty.commons.NettyCommonProtocol.REQUEST;
//
///**
// * 由MyBatis Generator工具自动生成
// */
//@SpringBootApplication
//@ComponentScan(basePackages={"com.mxny.ss","com.mxny.ss.netty.client"})
//@RestfulScan({"com.mxny.ss.netty.client.rpc"})
//@DTOScan(value={"com.mxny.ss.netty.dto"})
///**
// * 除了内嵌容器的部署模式，Spring Boot也支持将应用部署至已有的Tomcat容器, 或JBoss, WebLogic等传统Java EE应用服务器。
// * 以Maven为例，首先需要将<packaging>从jar改成war，然后取消spring-boot-maven-plugin，然后修改Application.java
// * 继承SpringBootServletInitializer
// */
//public class ClientApplication extends SpringBootServletInitializer implements CommandLineRunner{
//
//    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);
//
//    public static void main(String[] args) {
//        SpringApplication.run(ClientApplication.class, args);
//    }
//
//    @Override
//    public void run(String... strings) {
//        DefaultCommonClientConnector clientConnector = new DefaultCommonClientConnector();
//        Channel channel = clientConnector.connect(20011, "127.0.0.1");
//        ClientCache.channel = channel;
//        ClientCache.clientConnector = clientConnector;
//        MsgBody msgBody = new MsgBody();
//        msgBody.setId(6L);
//        msgBody.setName("客户端启动发送");
//        Message message = new Message();
//        message.sign(REQUEST);
//        message.data(msgBody);
//        //获取到channel发送双方规定的message格式的信息
//        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
//
//            @Override
//            public void operationComplete(ChannelFuture future) throws Exception {
//                if(!future.isSuccess()) {
//                    logger.info("send fail,reason is {}", future.cause().getMessage());
//                }
//            }
//        });
//        //防止对象处理发生异常的情况
//        DefaultCommonClientConnector.MessageNonAck msgNonAck = new DefaultCommonClientConnector.MessageNonAck(message, channel);
//        clientConnector.addNeedAckMessageInfo(msgNonAck);
//    }
//}
