package com.mxny.ss.netty.commons.channelhandler;

import com.mxny.ss.netty.commons.Acknowledge;
import com.mxny.ss.netty.commons.NettyCommonProtocol;
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
    protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out) throws Exception {
        byte[] bytes = SerializerHolder.serializerImpl().writeObject(ack);
        out.writeShort(NettyCommonProtocol.MAGIC)
                .writeByte(NettyCommonProtocol.ACK)
                .writeByte(0)
                .writeLong(ack.sequence())
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }
}
