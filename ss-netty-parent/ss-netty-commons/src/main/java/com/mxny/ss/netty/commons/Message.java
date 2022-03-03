package com.mxny.ss.netty.commons;

import com.mxny.ss.netty.commons.consts.NettyProtocolConsts;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发布订阅信息的包装类.
 *
 */
public class Message {

    private static final AtomicInteger sequenceGenerator = new AtomicInteger(0);
    /** 消息序号 */
    private final int sequence;
    /** 终端id */
    private int terminalId;
    /**
     * 消息类型
     * 参考NettyCommonProtocol
     */
    private short cmd;
    /** 帧来源, 1:server; 2:client */
    private byte source;
    /** 传输类型， 1:请求帧；2:应答帧; */
    private byte transferType;
    /** 终端时间(2020年1月1日0时开始的毫秒数) */
    private int terminalTime;

    private Object data;

    public Message() {
        this(sequenceGenerator.getAndIncrement());
    }

    public Message(int sequence) {
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public short getCmd() {
        return cmd;
    }

    public void setCmd(short cmd) {
        this.cmd = cmd;
    }

    public int getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(int terminalId) {
        this.terminalId = terminalId;
    }

    public byte getSource() {
        return source;
    }

    public void setSource(byte source) {
        this.source = source;
    }

    public byte getTransferType() {
        return transferType;
    }

    public void setTransferType(byte transferType) {
        this.transferType = transferType;
    }

    public int getTerminalTime() {
        return terminalTime;
    }

    public void setTerminalTime(int terminalTime) {
        this.terminalTime = terminalTime;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "sequence=" + sequence +
                ", terminalId=" + terminalId +
                ", cmd=" + cmd +
                ", source=" + source +
                ", transferType=" + transferType +
                ", terminalTime=" + DateFormatUtils.format(new Date(NettyProtocolConsts.TERMINAL_START_TIME + terminalTime), "yyyy-MM-dd HH:mm:ss") +
                ", data=" + data +
                '}';
    }
}
