package com.mxny.ss.netty.commons;

/**
 * ACK确认
 *
 */
public class Acknowledge {
    public Acknowledge() {}

    public Acknowledge(int sequence) {
        this.sequence = sequence;
    }
    // ACK序号
    private int sequence;

    public int sequence() {
        return sequence;
    }

    public void sequence(int sequence) {
        this.sequence = sequence;
    }
}
