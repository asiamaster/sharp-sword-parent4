# 自定义TCP传输协议
# 1 协议格式
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

# 2 命令字说明
| 命令码  | 命令项  | 备注  |
| ------------ | ------------ | ------------ |
| 1  | 心跳  |   |
| 2  | 登录  |   |
| 3  | 登出  |   |
| 4  | 服务端读数据  |收到上传心跳数据后发送(因为不知道有没有唤醒，休眠中是收不到数据的)   |
| 5  | 服务端写数据  |  同上 |
| 6  | 终端主动上报数据  | 数据上报，异常上报 |
| 7  | 终端读服务端时间  | 查询服务端现实时间，然后更新终端时间 |
| 8  | 终端向注册中心获取服务端登录IP地址  | 查询服务端IP和端口 |
| 9  | 服务端回复ACK  |  |

# 3 传输类型说明
1个字节: 0代表请求帧，1代表应答帧， 2代表主动上报
只要是服务端先发起的就用0，终端先发起的就用2， 回复都是1.

# 4 通讯数据格式
## 4.1 心跳(刷存在感)
| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 1  |
|4 | source |  byte | 1 | 2  |
|5 | transferType |  byte |  1 | 1  |
|6 | terminalTime | int  | 4  |  2020年1月1日0时开始的秒数  |
|7 | bodyLength | short  | 2  |  0  |

## 4.2 登录
| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 2  |
|4 | source |  byte | 1 | 2  |
|5 | transferType |  byte |  1 | 1  |
|6 | terminalTime | int  | 4  |  2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 终端编码字符串 |

## 4.3 登出
| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 3  |
|4 | source |  byte | 1 | 2  |
|5 | transferType |  byte |  1 | 1  |
|6 | terminalTime | int  | 4  |  2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 终端编码字符串 |

## 4.4 服务端读取终端数据
| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 4 服务端读数据 |
|4 | source |  byte | 1 | 1 服务端 |
|5 | transferType |  byte |  1 | 1 请求帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 消息内容, protobuf序号化 |

读取数据回应帧：

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 4 服务端读数据 |
|4 | source |  byte | 1 | 2 客户端  |
|5 | transferType |  byte |  1 | 2 应答帧 |
|6 | terminalTime | int  | 4  |  2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 消息内容, protobuf序号化 |

## 4.5 服务端写入终端数据
| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 5 服务端写数据 |
|4 | source |  byte | 1 | 1 服务端 |
|5 | transferType |  byte |  1 | 1 请求帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 消息内容, protobuf序号化 |

读取数据回应帧：

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 5 服务端写数据 |
|4 | source |  byte | 1 | 2 客户端  |
|5 | transferType |  byte |  1 | 2 应答帧 |
|6 | terminalTime | int  | 4  |  2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 消息内容, protobuf序号化 |

## 4.6 终端主动上报数据

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 6 终端上报数据 |
|4 | source |  byte | 1 | 2 客户端 |
|5 | transferType |  byte |  1 | 1 请求帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 消息内容, protobuf序号化 |

服务端应答帧:

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 6 终端上报数据 |
|4 | source |  byte | 1 | 1 服务端 |
|5 | transferType |  byte |  1 | 2 应答帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 消息内容, protobuf序号化 |

## 4.7 终端读服务端时间

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 7 终端读取服务端时间 |
|4 | source |  byte | 1 | 2 客户端 |
|5 | transferType |  byte |  1 | 1 请求帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  | 0  |

服务端应答帧:

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 7 终端读取服务端时间 |
|4 | source |  byte | 1 | 1 服务端 |
|5 | transferType |  byte |  1 | 2 应答帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的**秒**数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 1970年1月1日0时开始的**毫秒**数，protobuf序号化 |

## 4.8 终端读服务端地址(暂未支持)

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 8 终端向注册中心获取服务端登录IP地址 |
|4 | source |  byte | 1 | 2 客户端 |
|5 | transferType |  byte |  1 | 1 请求帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  | 0  |

注册中心端应答帧:

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 8 终端向注册中心获取服务端登录IP地址 |
|4 | source |  byte | 1 | 1 注册中心端 |
|5 | transferType |  byte |  1 | 2 应答帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的**秒**数 |
|7 | bodyLength | short  | 2  |   |
|8 | body | byte[]  | bodyLength  | 服务端地址host:port，protobuf序号化 |


## 4.9 服务端回复ACK

| 序号 |  协议头 |  类型 |  长度(字符) | 备注 |
| ------------ | ------------ | ------------ | ------------ |
|1 | magic  | short | 2 | 7857 |
|2 | terminalId |  int | 4 | 0  |
|3 | cmd    |  byte | 1 | 9 服务端回复ACK |
|4 | source |  byte | 1 | 1 服务端 |
|5 | transferType |  byte |  1 | 2 应答帧 |
|6 | terminalTime | int  | 4  | 2020年1月1日0时开始的秒数 |
|7 | bodyLength | short  | 2  |  |
|8 | body | byte[]  | bodyLength  | 应答对象，只有一个int属性的sequence字段，protobuf序号化 |


