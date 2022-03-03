# Client端开发说明
## 配置说明， 参考ClientConsts
```properties
#服务端数据处理器，ChannelInboundHandlerAdapter.channelRead方法
ss.netty.client.handler=com.mxny.netty.client.handler.ClientMessageHandler
#客户端解码器key(类全名)，多个以逗号分隔,下面为默认的NettyCommonProtocol协议实现
ss.netty.client.decoders=com.mxny.ss.netty.commons.channelhandler.MessageDecoder
#客户端编码器key(类全名)，多个以逗号分隔,下面为默认的NettyCommonProtocol协议实现
ss.netty.server.encoders=com.mxny.ss.netty.commons.channelhandler.MessageEncoder,com.mxny.ss.netty.commons.channelhandler.AcknowledgeEncoder
```

## Client端启动
```java
DefaultCommonClientConnector clientConnector = new DefaultCommonClientConnector();
Channel channel = clientConnector.connect(20011, "127.0.0.1", false);
ClientCache.channel = channel;
ClientCache.clientConnector = clientConnector;
```

## 客户端缓存ClientCache
`ClientCache.channel`保存和服务端的连接通道，连接服务端时初始化
`ServerCache.clientConnector`保存连接器，连接服务端时初始化
`ServerCache.messagesNonAcks`保存未确认的消息缓存，在ChannelInboundHandlerAdapter.channelRead收到Acknowledge类型的消息时，需要根据消息sequence移除

## 发送消息示例代码:
```java
Message message = new Message();
message.sign(LOGIN);
String terminalId = "1";
message.data(terminalId);
//获取到channel发送双方规定的message格式的信息
channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
@Override
public void operationComplete(ChannelFuture future) {
        if(!future.isSuccess()) {
        logger.info("send fail,reason is {}", future.cause().getMessage());
        }
    }
});
//防止对象处理发生异常的情况
MessageNonAck msgNonAck = new MessageNonAck(message, channel);
clientConnector.addNeedAckMessageInfo(msgNonAck);
```
## 接收消息的示例代码:
```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if(msg instanceof Acknowledge){
    logger.info("收到server端的Ack信息，无需再次发送信息, sequence:{}", ((Acknowledge)msg).sequence());
    ClientCache.messagesNonAcks.remove(((Acknowledge)msg).sequence());
    }else{
        System.out.println("客户端收到消息:"+ msg);
    }
}
```

## 协议格式
| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 起始帧, 7857 |
|2 | terminalId |  int | 4 | 终端号  |
|3 | cmd    |  byte | 1 | 命令字，参考命令字说明  |
|4 | source |  byte | 1 | 帧来源, 1:server; 2:client  |
|5 | transferType |  byte |  1 | 传输类型， 1:请求帧；2:应答帧;  |
|6 | terminalTime | int  | 4  |  终端时间(2020年1月1日0时开始的秒数)  |
|7 | bodyLength | short  | 2  |  消息长度, 最大65535  |
|8 | body | byte[]  | bodyLength   | 消息内容, protobuf序号化  |

# 命令字说明
| 命令码  | 命令项  | 备注  |
| ------------ | ------------ | ------------ |
| 1  | 心跳  | -  |
| 2  | 登录  | -  |
| 3  | 登出  | -  |
| 4  | 服务端读数据  |收到上传心跳数据后发送(因为不知道有没有唤醒，休眠中是收不到数据的)   |
| 5  | 服务端写数据  |  同上 |
| 6  | 终端主动上报数据  | 数据上报，异常上报 |
| 7  | 终端读服务端时间  | 查询服务端现实时间，然后更新终端时间 |
| 8  | 终端向注册中心获取服务端登录IP地址  | 查询服务端IP和端口 |
| 9  | 服务端回复ACK  | - |
