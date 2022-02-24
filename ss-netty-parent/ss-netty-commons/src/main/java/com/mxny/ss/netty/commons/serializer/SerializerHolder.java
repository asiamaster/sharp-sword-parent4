
package com.mxny.ss.netty.commons.serializer;

import com.mxny.ss.netty.commons.serializer.protostuff.ProtoStuffSerializer;


/**
 * 
 * @author Wangmi
 * @description 序列化工具 obj to byte 用于网络传输
 * @modifytime
 */
public final class SerializerHolder {

	//使用google的protostuff
	//protostuff 是一个支持各种格式的一个序列化Java类库，包括 JSON、XML、YAML等格式。
    private static final Serializer serializer = new ProtoStuffSerializer();

    public static Serializer serializerImpl() {
        return serializer;
    }
}
