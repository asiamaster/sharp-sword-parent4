package com.mxny.ss.netty.commons.channelhandler;

import com.mxny.ss.netty.commons.Message;
import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.mxny.ss.netty.commons.serializer.SerializerHolder.serializerImpl;

/**
 * 消息解码器
 * @author: WangMi
 * @time: 2022/3/3 11:15
 */
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) {
        byte[] bytes = serializerImpl().writeObject(msg);
        int terminalTime = msg.getTerminalTime() <= 0 ?
                new Long((System.currentTimeMillis() - NettyProtocolConsts.TERMINAL_START_TIME)/1000).intValue() :
                msg.getTerminalTime();
        //起始帧2
        out.writeShort(NettyProtocolConsts.MAGIC)
                //终端号4
                .writeInt(msg.getTerminalId())
                //命令字1
                .writeByte(msg.getCmd())
                //帧来源1, 1:server; 2:client
                .writeByte(NettyProtocolConsts.SOURCE_SERVER)
                //传输类型1， 1:请求帧；2:应答帧;
                .writeByte(NettyProtocolConsts.TRANSFER_TYPE_REQUEST)
                //终端时间4(2020年1月1日0时开始的秒数)
                .writeInt(terminalTime)
                //消息长度2, 最大65535
                .writeShort(bytes.length)
                //消息体
                .writeBytes(bytes);
//			//结束标识1, 0x68
//			.writeByte(NettyProtocolConsts.END_MARK)
//			//校验和， 从起始字符到结束标识所有的数据累加和
//			.writeInt(NettyProtocolConsts.PROTOCOL_HEADER_LENGTH + bytes.length);

    }
}
