package com.mxny.ss.netty.client.connector;

import com.mxny.ss.netty.commons.Heartbeats;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳trigger
 * @author WangMi
 */
@ChannelHandler.Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(ConnectorIdleStateTrigger.class);

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
//            	logger.info("need send heartbeats");
                ctx.writeAndFlush(Heartbeats.heartbeatContent());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
