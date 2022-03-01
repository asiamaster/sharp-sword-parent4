# Server端开发说明
## 配置说明
```properties
#配置服务端数据处理器，该类需要实现SimpleChannelInboundHandler<Message>.channelRead0方法
ss.netty.server.handler=com.mxny.netty.server.handler.ServerMessageHandler
```

# Server端事件监听器
实现`com.mxny.ss.netty.server.acceptor.ChannelEventListener`接口

# Server端启动
```java
DefaultCommonSrvAcceptor defaultCommonSrvAcceptor = new DefaultCommonSrvAcceptor(20011,new ServerChannelEventListener());
defaultCommonSrvAcceptor.start();
```

# 服务端缓存ServerCache
`ServerCache.TERMINAL_CHANNEL_MAP`保存终端号和ChannelId的关系，设备上线时赋值 用于Server向终端直接下发命令(一般是心跳命令和直接回复命令), 客户端断开时清空
`ServerCache.CHANNELID_TERMINAL_MAP`保存ChannelId和终端号的关系，设备上线时赋值 在设备第一次连接时更新，客户端断开时清空

# 协议
                                        Protocol
     *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
     *       2   │   1   │    1   │     8     │      4      │
     *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
     *           │       │        │           │             │
     *  │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
     *           │       │        │           │             │
     *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘