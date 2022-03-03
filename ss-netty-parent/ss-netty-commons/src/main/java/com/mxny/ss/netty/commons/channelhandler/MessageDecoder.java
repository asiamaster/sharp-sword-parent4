package com.mxny.ss.netty.commons.channelhandler;

import com.mxny.ss.netty.commons.Acknowledge;
import com.mxny.ss.netty.commons.Message;
import com.mxny.ss.netty.commons.NettyCommonProtocol;
import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.mxny.ss.netty.commons.serializer.SerializerHolder.serializerImpl;

/**
 * | 序号 |  协议头 |  类型 |  长度(字符) |   描述 |
 * | ------------ | ------------ | ------------ | ------------ |
 * |1 | magic  | short | 2 | 起始帧, 7857 |
 * |2 | terminalId |  int | 4 | 终端号  |
 * |3 | cmd    |  byte | 1 | 命令字，参考命令字说明  |
 * |4 | source |  byte | 1 | 帧来源, 1:server; 2:client  |
 * |5 | transferType |  byte |  1 | 传输类型， 1:请求帧；2:应答帧;  |
 * |6 | terminalTime | int  | 4  |  终端时间(2020年1月1日0时开始的毫秒数)  |
 * |7 | bodyLength | short  | 2  |  消息长度, 最大65535  |
 * |8 | body | byte[]  | bodyLength   | 消息内容, protobuf序号化  |
 * 解码器
 * @author: WangMi
 * @time: 2022/3/3 11:42
 */
public class MessageDecoder extends ReplayingDecoder<MessageDecoder.State> {

    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    public MessageDecoder() {
        super(State.HEADER_MAGIC);
    }

    // 协议头
    private final NettyCommonProtocol header = new NettyCommonProtocol();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            switch (state()) {
                case HEADER_MAGIC:// 1.MAGIC
                    checkMagic(in.readShort());
                    checkpoint(State.HEADER_TERMINAL_ID);
                case HEADER_TERMINAL_ID: // 2.终端号或sequence
                    header.setTerminalId(in.readInt());
                    checkpoint(State.HEADER_CMD);
                case HEADER_CMD:// 3. 命令字
                    header.setCmd(in.readByte());
                    checkpoint(State.HEADER_SOURCE);
                case HEADER_SOURCE:// 4.帧来源
                    header.setSource(in.readByte());
                    checkpoint(State.HEADER_TRANSFER_TYPE);
                case HEADER_TRANSFER_TYPE:// 5.传输类型
                    header.setTransferType(in.readByte());
                    checkpoint(State.HEADER_TERMINAL_TIME);
                case HEADER_TERMINAL_TIME:// 6.终端时间(2020年1月1日0时开始的秒数)
                    header.setTerminalTime(in.readInt());
                    checkpoint(State.HEADER_BODY_LENGTH);
                case HEADER_BODY_LENGTH:// 7.消息体长度
                    header.setBodyLength(in.readShort());
                    checkpoint(State.BODY);
                case BODY: //8. 消息体
                    switch (header.getCmd()) {
                        case NettyProtocolConsts.CMD_HEARTBEAT:
                            break;
                        case NettyProtocolConsts.CMD_LOGIN:
                        case NettyProtocolConsts.CMD_LOGOUT:
                        case NettyProtocolConsts.CMD_SERVER_READ:
                        case NettyProtocolConsts.CMD_SERVER_WRITE:
                        case NettyProtocolConsts.CMD_TERMINAL_REPORT:
                        case NettyProtocolConsts.CMD_TERMINAL_REQUEST_TIME:
                        {
                            byte[] bytes = new byte[header.getBodyLength()];
                            in.readBytes(bytes);
                            Message msg = serializerImpl().readObject(bytes, Message.class);
                            msg.setCmd(header.getCmd());
                            msg.setSource(header.getSource());
                            msg.setTransferType(header.getTransferType());
                            msg.setTerminalId(header.getTerminalId());
                            msg.setTerminalTime(header.getTerminalTime());
                            out.add(msg);
                            break;
                        }
                        case NettyProtocolConsts.CMD_ACK: {
                            byte[] bytes = new byte[header.getBodyLength()];
                            in.readBytes(bytes);
                            Acknowledge ack = serializerImpl().readObject(bytes, Acknowledge.class);
                            out.add(ack);
                            break;
                        }
                        default:
                            throw new IllegalAccessException("不支持的命令字");
                    }
                    checkpoint(State.HEADER_MAGIC);
                    break;
                default:
                    throw new IllegalAccessException("不支持的协议");
//                case END_MARK:// 9.结束标识
//                    header.setEndMark(in.readByte());
//                    checkpoint(State.CHECK_SUM);
//                case CHECK_SUM:// 10.校验和
//                    header.setCheckSum(in.readInt());
//                    checkpoint(State.HEADER_MAGIC);
            }
        } catch (Exception e) {
            logger.error("协议解析失败:"+e.getMessage());
            releaseBuf(in);
        }
    }

    /**
     * 重置缓存，并checkpoint到开始帧
     * @param in
     */
    private void releaseBuf(ByteBuf in) {
        //重置到开始帧
        checkpoint(State.HEADER_MAGIC);
        //跳过未读的部分
        in.skipBytes(in.writerIndex() - in.readerIndex());
    }

    private static void checkMagic(short magic) {
        if (NettyProtocolConsts.MAGIC != magic) {
            throw new IllegalArgumentException("开始帧错误");
        }
    }

    enum State {
        HEADER_MAGIC,
        HEADER_TERMINAL_ID,
        HEADER_CMD,
        HEADER_SOURCE,
        HEADER_TRANSFER_TYPE,
        HEADER_TERMINAL_TIME,
        HEADER_BODY_LENGTH,
        BODY
//            END_MARK,
//            CHECK_SUM
    }
}
