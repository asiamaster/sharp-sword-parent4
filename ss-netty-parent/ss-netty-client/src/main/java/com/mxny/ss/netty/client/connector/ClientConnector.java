package com.mxny.ss.netty.client.connector;

import io.netty.channel.Channel;

/**
 * 
 * @author WangMi
 * @description
 */
public interface ClientConnector {
	
	Channel connect(int port,String host);
	
	void shutdownGracefully();
	
}
