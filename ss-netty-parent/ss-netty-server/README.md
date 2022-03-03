# Server端开发说明
## 配置说明， 参考ServerConsts
```properties
#服务端数据处理器key(类全名)，该类需要实现SimpleChannelInboundHandler<Message>.channelRead0方法
ss.netty.server.handler=com.mxny.netty.server.handler.ServerMessageHandler
#服务端解码器key(类全名)，多个以逗号分隔,下面为默认的NettyCommonProtocol协议实现
ss.netty.server.decoders=com.mxny.ss.netty.commons.channelhandler.MessageDecoder
#服务端编码器key(类全名)，多个以逗号分隔,下面为默认的NettyCommonProtocol协议实现
ss.netty.server.encoders=com.mxny.ss.netty.commons.channelhandler.MessageEncoder,com.mxny.ss.netty.commons.channelhandler.AcknowledgeEncoder
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

## 协议格式
| 序号 |  协议头 |  类型 |  长度(字符) |   描述 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 起始帧, 7857 |
|2 | terminalId |  int | 4 | 终端号  |
|3 | cmd    |  byte | 1 | 命令字，参考命令字说明  |
|4 | source |  byte | 1 | 帧来源, 1:server; 2:client  |
|5 | transferType |  byte |  1 | 传输类型， 1:请求帧；2:应答帧;  |
|6 | terminalTime | int  | 4  |  终端时间(2020年1月1日0时开始的毫秒数)  |
|7 | bodyLength | short  | 2  |  消息长度, 最大65535  |
|8 | body | byte[]  | bodyLength   | 消息内容, protobuf序号化  |

# 命令字说明
| 命令码  | 命令项  | 备注  |
| ------------ | ------------ | ------------ |
| 01  | 心跳  |   |
| 02  | 登录  |   |
| 03  | 登出  |   |
| 04  | 服务器读数据  |收到上传心跳数据后发送(因为不知道有没有唤醒，休眠中是收不到数据的)   |
| 05  | 服务器写数据  |  同上 |
| 06  | 终端主动上传数据  | 发现异常数据时上报  |
| 07  | 读服务器时间或登录IP地址  | 查询服务器现实时间，然后更新终端时间 |

# 传输类型说明
1个字节: 0代表请求帧，1代表应答帧， 2代表主动上报
只要是服务器先发起的就用0，终端先发起的就用2， 回复都是1.
