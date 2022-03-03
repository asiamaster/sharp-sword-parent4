package com.mxny.ss.netty.commons;

import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 
 * @author WangMi
 * @description 心跳包
 */
public class Heartbeats {

    private static final ByteBuf HEARTBEAT_BUF;

    static {
        ByteBuf buf = Unpooled.buffer(NettyProtocolConsts.PROTOCOL_HEADER_LENGTH);
        //起始帧2
        buf.writeShort(NettyProtocolConsts.MAGIC)
                //终端号4, 此处为消息序号
                .writeInt(0)
                //命令字1
                .writeByte(NettyProtocolConsts.CMD_HEARTBEAT)
                //帧来源1, 1:server; 2:client
                .writeByte(NettyProtocolConsts.SOURCE_CLIENT)
                //传输类型1， 1:请求帧；2:应答帧;
                .writeByte(NettyProtocolConsts.TRANSFER_TYPE_REQUEST)
                //终端时间4(2020年1月1日0时开始的毫秒数)
                .writeInt(0)
                //消息长度2, 最大65535
                .writeShort(0)
                //消息体
                .writeBytes(new byte[0]);
//        //结束标识1, 0x68
//        .writeByte(NettyProtocolConsts.END_MARK)
//        //校验和， 从起始字符到结束标识所有的数据累加和
//        .writeInt(NettyProtocolConsts.PROTOCOL_HEADER_LENGTH);
        HEARTBEAT_BUF = Unpooled.unmodifiableBuffer(Unpooled.unreleasableBuffer(buf));
    }

    public static ByteBuf heartbeatContent() {
        return HEARTBEAT_BUF.duplicate();
    }
}
