package com.mxny.ss.netty.client.connector;

import com.mxny.ss.netty.client.cache.ClientCache;
import com.mxny.ss.netty.client.consts.ClientConsts;
import com.mxny.ss.netty.client.dto.MessageNonAck;
import com.mxny.ss.netty.commons.Acknowledge;
import com.mxny.ss.netty.commons.ConnectionWatchdog;
import com.mxny.ss.netty.commons.NativeSupport;
import com.mxny.ss.netty.commons.channelhandler.AcknowledgeEncoder;
import com.mxny.ss.netty.commons.channelhandler.MessageDecoder;
import com.mxny.ss.netty.commons.channelhandler.MessageEncoder;
import com.mxny.ss.netty.commons.exception.ConnectFailedException;
import com.mxny.ss.util.SpringUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 
 * @author WangMi
 * @description 默认的一些比较常用的client的配置
 */
public class DefaultCommonClientConnector extends NettyClientConnector {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultCommonClientConnector.class);
	
	//每个连接维护一个channel
	private volatile Channel channel;
	
	//信息处理的handler
//	private final MessageHandler handler = new MessageHandler();
//	//编码
//    private final MessageEncoder encoder = new MessageEncoder();
//    //ack
//    private final AcknowledgeEncoder ackEncoder = new AcknowledgeEncoder();
    
	protected final HashedWheelTimer timer = new HashedWheelTimer(new ThreadFactory() {
		
		private AtomicInteger threadIndex = new AtomicInteger(0);
		
		@Override
        public Thread newThread(Runnable r) {
			return new Thread(r, "NettyClientConnectorExecutor_" + this.threadIndex.incrementAndGet());
		}
	});
	
	//心跳trigger
	private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

	public DefaultCommonClientConnector() {
		init();
	}

	@Override
	protected void init() {
		super.init();
		bootstrap().option(ChannelOption.ALLOCATOR, allocator)
		.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT)
		.option(ChannelOption.SO_REUSEADDR, true)
		.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) SECONDS.toMillis(3))
		.channel(NioSocketChannel.class);
		bootstrap().option(ChannelOption.SO_KEEPALIVE, true)
		.option(ChannelOption.TCP_NODELAY, true)
		.option(ChannelOption.ALLOW_HALF_CLOSURE, false);
	}

    /**
     * 客户端连接入口方法
     * @param port
     * @param host
     * @return
     */
    @Override
    public Channel connect(int port, String host) {
        return connect(port, host, true);
    }
    /**
     * 客户端连接入口方法
     * @param port
     * @param host
     * @return
     */
	@Override
    public Channel connect(int port, String host, boolean reconnect) {
		final Bootstrap boot = bootstrap();
		
        // 重连watchdog
        final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, port,host) {

            @Override
            public ChannelHandler[] handlers() {
                try {
                    List<ChannelHandler> channelHandlers = new ArrayList<>();
                    //将自己[ConnectionWatchdog]装载到handler链中，当链路断掉之后，会触发ConnectionWatchdog #channelInActive方法
                    channelHandlers.add(this);
                    //每隔30s的时间触发一次userEventTriggered的方法，并且指定IdleState的状态位是WRITER_IDLE
                    channelHandlers.add(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
                    //实现userEventTriggered方法，并在state是WRITER_IDLE的时候发送一个心跳包到sever端，告诉server端我还活着
                    channelHandlers.add(idleStateTrigger);
                    channelHandlers.addAll(getDecoders());
                    channelHandlers.addAll(getEncoders());
                    channelHandlers.add(getHandler());
                    return channelHandlers.toArray(new ChannelHandler[channelHandlers.size()]);
//                    return new ChannelHandler[] {
//                            //将自己[ConnectionWatchdog]装载到handler链中，当链路断掉之后，会触发ConnectionWatchdog #channelInActive方法
//                            this,
//                            //每隔30s的时间触发一次userEventTriggered的方法，并且指定IdleState的状态位是WRITER_IDLE
//                            new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
//                            //实现userEventTriggered方法，并在state是WRITER_IDLE的时候发送一个心跳包到sever端，告诉server端我还活着
//                            idleStateTrigger,
//                            new MessageDecoder(),
//                            encoder,
//                            ackEncoder,
//                            getHandler()
//                    };
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }};
        watchdog.setReconnect(reconnect);
        try {
            ChannelFuture future;
            synchronized (bootstrapLock()) {
                boot.handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(watchdog.handlers());
                    }
                });
                future = boot.connect("127.0.0.1", 20011);
            }
            future.sync();
            channel = future.channel();
        } catch (Throwable t) {
            throw new ConnectFailedException("connects to [" + host + ":"+port+"] fails", t);
        }
		return channel;
	}

    /**
     * 演示的消息处理器
     */
	@ChannelHandler.Sharable
    class MessageHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			if(msg instanceof Acknowledge){
				logger.debug("收到server端的Ack信息，无需再次发送信息, sequence:{}", ((Acknowledge)msg).sequence());
				ClientCache.messagesNonAcks.remove(((Acknowledge)msg).sequence());
			}else{
                System.out.println("客户端收到消息:"+ msg);
            }
		}

    }
	
	@Override
	protected EventLoopGroup initEventLoopGroup(int nWorkers, ThreadFactory workerFactory) {
		return NativeSupport.isSupportNativeET() ? new EpollEventLoopGroup(nWorkers, workerFactory) : new NioEventLoopGroup(nWorkers, workerFactory);
	}


    /**
     * 随DefaultCommonClientConnector初始化后启动
     */
    private class AckTimeoutScanner implements Runnable {
        //重试集合扫描频率
        private long sleepMsillis = 500;
        @Override
        public void run() {
            for (;;) {
                try {
                    for (MessageNonAck m : ClientCache.messagesNonAcks.values()) {
                        //十秒钟没有发出去， 判断是否已经发送，如果没有发送，则根据id移除messagesNonAcks中的缓存
                        // 判断连接是否有效，有效，则重新构建MessageNonAck并发送
                        if (System.currentTimeMillis() - m.getTimestamp() > SECONDS.toMillis(10)) {

                            // 移除
                            if (ClientCache.messagesNonAcks.remove(m.getId()) == null) {
                                continue;
                            }

                            if (m.getChannel().isActive()) {
                            	logger.warn("准备重新发送信息");
                                MessageNonAck msgNonAck = new MessageNonAck(m.getMsg(), m.getChannel());
                                ClientCache.messagesNonAcks.put(msgNonAck.getId(), msgNonAck);
                                m.getChannel().writeAndFlush(m.getMsg())
                                        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
                            }
                        }
                    }

                    Thread.sleep(sleepMsillis);
                } catch (Throwable t) {
                    logger.error("An exception has been caught while scanning the timeout acknowledges {}.", t);
                }
            }
        }
    }
	
	{
        Thread t = new Thread(new AckTimeoutScanner(), "ack.timeout.scanner");
        t.setDaemon(true);
        t.start();
    }

    /**
     * 添加MessageNonAck到缓存
     * @param msgNonAck
     */
	public void addNeedAckMessageInfo(MessageNonAck msgNonAck) {
		 ClientCache.messagesNonAcks.put(msgNonAck.getId(), msgNonAck);
	}

    /**
     * 获取解码器
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected List<ChannelHandler> getDecoders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String decoders = SpringUtil.getProperty(ClientConsts.CLIENT_DECODERS, MessageDecoder.class.getName());
        String[] split = decoders.split(",");
        List<ChannelHandler> channelHandlers = new ArrayList<>(split.length);
        for (String decoderStr : split) {
            channelHandlers.add(createInstance(ChannelHandler.class, decoderStr));
        }
        return channelHandlers;
    }

    /**
     * 获取编码器
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected List<ChannelHandler> getEncoders() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String decoders = SpringUtil.getProperty(ClientConsts.CLIENT_ENCODERS, MessageEncoder.class.getName()+"," +AcknowledgeEncoder.class.getName());
        String[] split = decoders.split(",");
        List<ChannelHandler> channelHandlers = new ArrayList<>(split.length);
        for (String encoder : split) {
            channelHandlers.add(createInstance(ChannelHandler.class, encoder));
        }
        return channelHandlers;
    }

    /**
     * 获取消息处理器
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    protected ChannelInboundHandlerAdapter getHandler() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String serverHandler = SpringUtil.getProperty(ClientConsts.CLIENT_HANDLER, MessageHandler.class.getName());
        return createInstance(ChannelInboundHandlerAdapter.class, serverHandler);
    }

    /**
     * 传递一个类的全新类名来创建对象
     * @param checkType
     * @param className
     * @param <T>
     * @return
     */
    private static  <T> T createInstance(Class<T> checkType,String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Class<T> clz = (Class<T>)Class.forName(className);
        Object obj = clz.newInstance();
        //需要检查checkType是不是obj的字节码对象
        if (!checkType.isInstance(obj)) {
            throw new ClassCastException("对象跟字节码不兼容");
        }
        return (T)obj;
    }

}
