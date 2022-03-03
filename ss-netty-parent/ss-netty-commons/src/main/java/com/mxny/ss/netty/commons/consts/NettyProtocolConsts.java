package com.mxny.ss.netty.commons.consts;

import com.mxny.ss.netty.commons.utils.DateUtils;

/**
 * Netty协议常量
 * @author: WangMi
 * @time: 2022/3/1 13:59
 */
public class NettyProtocolConsts {
    //  -------------------  消息体常量st  ---------------------
    public static final byte SOURCE_SERVER = 1;
    public static final byte SOURCE_CLIENT = 2;

    /** 协议头长度，不包括消息内容长度， 用于校验和 */
    public static final int PROTOCOL_HEADER_LENGTH = 20;

    /** Request */
    public static final byte TRANSFER_TYPE_REQUEST = 1;
    /** Response */
    public static final byte TRANSFER_TYPE_RESPONSE = 2;

    /** Heartbeat */
    public static final byte CMD_HEARTBEAT = 1;
    public static final byte CMD_LOGIN = 2;
    public static final byte CMD_LOGOUT = 3;
    /** 服务器读 */
    public static final byte CMD_SERVER_READ = 4;
    /** Response */
    public static final byte CMD_SERVER_WRITE = 5;
    /** 终端上报(数据上报，异常上报) */
    public static final byte CMD_TERMINAL_REPORT = 6;
    /** 终端读服务端时间 */
    public static final byte CMD_TERMINAL_REQUEST_TIME = 7;
    /** 终端读服务端地址 */
    public static final byte CMD_TERMINAL_REQUEST_ADDR = 8;
    /** 服务端回复Acknowledge */
    public static final byte CMD_ACK = 9;
    /** Magic */
    public static final short MAGIC = 7857;
    //终端起始时间
    public static final long TERMINAL_START_TIME = DateUtils.formatDateStr2Date("2020-01-01 00:00:00").getTime();
//    //结束标识
//    public static final byte END_MARK = 68;
//  -------------------  消息体常量end ---------------------

}
