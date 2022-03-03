package com.mxny.ss.netty.commons.channelhandler;

import com.mxny.ss.netty.commons.Acknowledge;
import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
import com.mxny.ss.netty.commons.serializer.SerializerHolder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 
 * @author WangMi
 * @description ack的编码器
 */
@ChannelHandler.Sharable
public class AcknowledgeEncoder extends MessageToByteEncoder<Acknowledge> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out) {
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(ack);
        //起始帧2
        out.writeShort(NettyProtocolConsts.MAGIC)
        //终端号4, 此处为消息序号
        .writeInt(ack.sequence())
        //命令字1
        .writeByte(NettyProtocolConsts.CMD_ACK)
        //帧来源1, 1:server; 2:client
        .writeByte(NettyProtocolConsts.SOURCE_SERVER)
        //传输类型1， 1:请求帧；2:应答帧;
        .writeByte(NettyProtocolConsts.TRANSFER_TYPE_RESPONSE)
        //终端时间4(2020年1月1日0时开始的毫秒数)
        .writeInt(0)
        //消息长度2, 最大65535
        .writeShort(bytes.length)
        //消息体
        .writeBytes(bytes);
//        //结束标识1, 0x68
//        .writeByte(NettyProtocolConsts.END_MARK)
//        //校验和， 从起始字符到结束标识所有的数据累加和
//        .writeInt(NettyProtocolConsts.PROTOCOL_HEADER_LENGTH + bytes.length);
    }
}
