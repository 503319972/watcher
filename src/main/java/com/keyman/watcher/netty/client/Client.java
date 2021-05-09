package com.keyman.watcher.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private EventLoopGroup worker;
    private volatile boolean start = false;
    private int port;
    private String ip;
    private final ClientInitializer clientInitializer;

    public EventLoopGroup getWorker() {
        return worker;
    }

    public boolean isStart() {
        return start;
    }

    public Client(String ip, int port) {
        this.ip = ip;
        this.port = port;
        clientInitializer = new ClientInitializer();
        flush();
        bootstrap.group(worker)
                // avoid TCP Nagle to send tiny data
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(clientInitializer);
    }

    private void flush() {
        this.worker = new NioEventLoopGroup();
    }

    public Client() {
        this("127.0.0.1", 58898);
    }

    public Client(String ip) {
        this(ip, 58898);
    }

    public void setHandler(Consumer<Object> handler){
        clientInitializer.setDataHandler(handler);
    }

    public void setServerDownHandler(Consumer<Void> handler){
        clientInitializer.setServerDownHandler(handler);
    }

    public void toStart() {
        flush();
        try {
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            ChannelFuture cf = future.channel().closeFuture();
            start = true;
            cf.sync();
        }  catch (InterruptedException e) {
            log.error("cannot build connection", e);
            close();
            Thread.currentThread().interrupt();
        }
    }

    public void close() {
        if (start) {
            log.info("close client");
            worker.shutdownGracefully();
            start = false;
        }
    }

    public void start(){
        if (!start) {
            toStart();
        }
    }

    public void start(boolean force){
        if (force) {
            close();
            toStart();
        } else {
            start();
        }
    }
}
