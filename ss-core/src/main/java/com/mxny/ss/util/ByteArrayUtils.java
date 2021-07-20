package com.mxny.ss.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * byte数组工具
 */
public class ByteArrayUtils {

    /**
     * bytes转shorts, 由低到高即低地址存放低字节
     * 两个byte为一个short
     * @param bytes
     * @return
     */
    public static short[] bytes2short(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    /**
     * short转bytes, 由高到低
     * @param shorts
     * @return
     */
    public static byte[] short2bytes(Short shorts) {
        if(shorts == null){
            return null;
        }
        byte[] bytes = new byte[2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }

    /**
     * bytes转shorts, 由高到低即高地址存放低字节
     * 两个byte为一个short
     * @param bytes
     * @return
     */
    public static short[] bytes2short2(byte[] bytes) {
        if(bytes == null){
            return null;
        }
        short[] shorts = new short[bytes.length/2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    /**
     * shorts转bytes, 由低到高
     * @param shorts
     * @return
     */
    public static byte[] short2bytes2(Short shorts) {
        if(shorts == null){
            return null;
        }
        byte[] bytes = new byte[2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().put(shorts);
        return bytes;
    }

    /**
     * int --> byte[] 整形转byte[]
     * 比如int:1则是{1,0,0,0}
     * @param res
     * @return
     */
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    /**
     * byte[] -->int byte[]转整形
     * @param res
     * @return
     */
    public static int byte2int(byte[] res) {
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示按位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    /**
     * int到byte[] 由高位到低位
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] int2byte2(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    /**
     * byte[]转int 底位到高位
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byte2int2(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static String toHexString(byte[] ba) {
        StringBuilder sbuf = new StringBuilder();
        for (byte b : ba) {
            String s = Integer.toHexString((int) (b & 0xff));
            if (s.length() == 1) {
                sbuf.append('0');
            }
            sbuf.append(s);
        }
        return sbuf.toString();
    }

    public static byte[] hexString2bytearray(String s) {
        int len = s.length();
        byte[] ba = new byte[len / 2];

        for (int i = 0; i < ba.length; i++) {
            int j = i * 2;
            int t = Integer.parseInt(s.substring(j, j + 2), 16);
            byte b = (byte) (t & 0xFF);
            ba[i] = b;
        }
        return ba;
    }

    public static byte[] toPrimitives(Byte[] objBytes)
    {
        byte[] bytes = new byte[objBytes.length];
        for(int i = 0; i < objBytes.length; i++) {
            bytes[i] = objBytes[i];
        }
        return bytes;
    }

    // byte[] to Byte[]
    public static Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];
        int i = 0;
        for (byte b : bytesPrim) {
            // Autoboxing
            bytes[i++] = b;
        }
        return bytes;
    }
}
