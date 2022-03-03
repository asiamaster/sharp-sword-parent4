package com.mxny.ss.netty.commons;

/**
 * Netty的C/S端的之间约定的协议
 * @author wm
 */
public class NettyCommonProtocol {

    /**
     * **************************************************************************************************
     *                                          Protocol
     *  | 序号 |  协议头 |  类型 |  长度(字符) |   描述 |
     * | ------------ | ------------ | ------------ | ------------ |
     * |1 | magic  | short | 2 | 起始帧, 7857 |
     * |2 | terminalId |  int | 4 | 终端号  |
     * |3 | cmd    |  byte | 1 | 命令字，参考命令字说明  |
     * |4 | source |  byte | 1 | 帧来源, 1:server; 2:client  |
     * |5 | transferType |  byte |  1 | 传输类型， 1:请求帧；2:应答帧;  |
     * |6 | terminalTime | int  | 4  |  终端时间(2020年1月1日0时开始的毫秒数)  |
     * |7 | bodyLength | short  | 2  |  消息长度, 最大65535  |
     * |8 | body | byte[]  | bodyLength   | 消息内容, protobuf序号化  |
     */

    /** 命令字，参考命令字说明 */
    private byte cmd;
    /** 终端id */
    private int terminalId;
    /** 帧来源, 1:server; 2:client */
    private byte source;
    /** 传输类型， 1:请求帧；2:应答帧; */
    private byte transferType;
    /** 终端时间(2020年1月1日0时开始的毫秒数) */
    private int terminalTime;
    /** 消息体长度 */
    private short bodyLength;
//    /** 结束标识, 0x68 */
//    private byte endMark;
//    /** 校验和，从起始字符到结束标识所有的数据累加和 */
//    private int checkSum;

    public NettyCommonProtocol() {
    }

    public byte getCmd() {
        return cmd;
    }

    public void setCmd(byte cmd) {
        this.cmd = cmd;
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

    public short getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(short bodyLength) {
        this.bodyLength = bodyLength;
    }

    public int getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(int terminalId) {
        this.terminalId = terminalId;
    }

    @Override
	public String toString() {
		return "NettyCommonProtocol [cmd=" + cmd + ", source=" + source + ", transferType=" + transferType + ", bodyLength=" + bodyLength + "]";
	}

}
