# Client端开发说明
## 配置说明
```properties
#配置服务端数据处理器，ChannelInboundHandlerAdapter.channelRead方法
ss.netty.client.handler=com.mxny.netty.client.handler.ClientMessageHandler
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

## 协议
                                        Protocol
     *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     *       2   │   1   │    1   │     8     │      4      │
     *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
     *           │       │        │           │             │
     *  │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
     *           │       │        │           │             │
     *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘