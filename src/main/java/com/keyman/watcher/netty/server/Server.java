package com.keyman.watcher.netty.server;

import com.keyman.watcher.netty.NettyConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup boss;
    private EventLoopGroup work;
    private final ServerInHandler channelHandler;
    private final Integer port;
    private final LengthFieldPrepender lengthFieldPrepender = new LengthFieldPrepender(2);
    private final StringEncoder stringEncoder = new StringEncoder();

    private final ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
        @Override
        protected void initChannel(SocketChannel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(NettyConfig.getMaxFrameLength()
                    , 0, 2, 0, 2));
            pipeline.addLast(lengthFieldPrepender);
            pipeline.addLast(stringEncoder);
            pipeline.addLast(new ByteArrayDecoder());
            pipeline.addLast(channelHandler);
        }
    };
    private volatile boolean start = false;


    public Server(NettyConfig nettyConfig, BiConsumer<ChannelHandlerContext, Object> react) {
        this.port = nettyConfig.getPort();
        this.channelHandler = new ServerInHandler(react);
    }

    public void setReact(Consumer<ChannelHandlerContext> react) {
        channelHandler.setReact(react);
    }

    public void toClose() {
        LOGGER.info("close server....");
        boss.shutdownGracefully();
        work.shutdownGracefully();
        start = false;
    }

    @PreDestroy
    public void close() {
        if (start) {
            toClose();
        } else {
            LOGGER.warn("server have already closed.");
        }
    }

    private void flush(){
        serverBootstrap = new ServerBootstrap();
        this.boss = new NioEventLoopGroup();
        this.work = new NioEventLoopGroup();
    }

    public void toStart() {
        flush();
        serverBootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                // tcp keepalive to send a data patch
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler(LogLevel.INFO));
        try {
            serverBootstrap.childHandler(channelInitializer);
            LOGGER.info("netty listening {}", port);
            ChannelFuture f = serverBootstrap.bind(port).sync();
            ChannelFuture channelFuture = f.channel().closeFuture();
            start = true;
            channelFuture.sync();
        } catch (Exception e) {
            LOGGER.info("release resources due to", e);
            close();
        }
    }

    public void start() {
        if (!start) {
            toStart();
        } else {
            LOGGER.warn("server have already started.");
        }
    }
}
