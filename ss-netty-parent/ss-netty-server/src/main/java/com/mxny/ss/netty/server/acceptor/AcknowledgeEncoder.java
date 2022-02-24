package com.mxny.ss.netty.server.acceptor;

import com.mxny.ss.netty.commons.Acknowledge;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.mxny.ss.netty.commons.NettyCommonProtocol.ACK;
import static com.mxny.ss.netty.commons.NettyCommonProtocol.MAGIC;
import static com.mxny.ss.netty.commons.serializer.SerializerHolder.serializerImpl;


/**
 * 
 * @author Wang Mi
 * @description ack的编码器
 */
@ChannelHandler.Sharable
public class AcknowledgeEncoder extends MessageToByteEncoder<Acknowledge> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Acknowledge ack, ByteBuf out) {
        byte[] bytes = serializerImpl().writeObject(ack);
        out.writeShort(MAGIC)
                .writeByte(ACK)
                .writeByte(0)
                .writeLong(ack.sequence())
                .writeInt(bytes.length)
                .writeBytes(bytes);
    }
}
