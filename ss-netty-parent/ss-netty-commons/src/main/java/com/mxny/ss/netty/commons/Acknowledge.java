package com.mxny.ss.netty.commons;

/**
 * ACK确认
 *
 */
public class Acknowledge {
    //命令类型，登录，登出，消息
    public byte cmd;
    public Acknowledge() {}

    public Acknowledge(long sequence, byte cmd) {
        this.sequence = sequence;
        this.cmd = cmd;
    }
    // ACK序号
    private long sequence;

    public long sequence() {
        return sequence;
    }

    public void sequence(long sequence) {
        this.sequence = sequence;
    }
}
