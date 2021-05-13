package com.keyman.watcher.netty.client;

import com.keyman.watcher.exception.NettyException;
import com.keyman.watcher.netty.NettyConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private EventLoopGroup worker;
    private volatile boolean start = false;
    private final CopyOnWriteArrayList<Channel> connectedChannels = new CopyOnWriteArrayList<>();
    private final NettyConfig config;
    private final ClientInitializer clientInitializer;

    public EventLoopGroup getWorker() {
        return worker;
    }

    public List<Channel> getConnectedChannels() {
        return connectedChannels;
    }

    public boolean isStart() {
        return start;
    }

    public Client(NettyConfig config) {
        this.config = config;
        clientInitializer = new ClientInitializer();
        flush();
        bootstrap.group(worker)
                // avoid TCP Nagle to send tiny data
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(clientInitializer);
    }

    private void flush() {
        this.worker = new NioEventLoopGroup();
    }

    public Client() {
        this(new NettyConfig(Collections.singletonList("127.0.0.1")));
    }

    public Client(String ip) {
        this(new NettyConfig(Collections.singletonList(ip)));
    }

    public Client(List<String> hosts) {
        this(new NettyConfig(hosts));
    }

    public void setHandler(BiConsumer<ChannelHandlerContext,Object> dataHandler){
        clientInitializer.setDataHandler(dataHandler);
    }

    public void setServerDownHandler(Consumer<Void> handler){
        clientInitializer.setServerDownHandler(handler);
    }

    public void toStart() {
        flush();
        config.getHosts().forEach(ip -> {
            bootstrap.remoteAddress(ip, config.getPort());
            ChannelFuture future = bootstrap.connect().addListener((ChannelFuture channelFuture) -> {
                if (!channelFuture.isSuccess()) {
                    log.warn("connect failed to {}, remove this channel", ip);
                    connectedChannels.remove(channelFuture.channel());
                }
            });
            connectedChannels.add(future.channel());
        });
        start = true;
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

    public void sendMsg(@NotNull Channel channel, byte[] msg) {
        if (!channel.isActive()) {
            reconnect(channel);
        }
        try {
            if (channel.isActive()) {
                InetSocketAddress ipSocket = (InetSocketAddress) channel.remoteAddress();
                String host = ipSocket.getHostString();
                int curPort = ipSocket.getPort();
                log.info("send data to {}:{}", host , curPort);
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg);
                channel.writeAndFlush(buf).sync();
            }
        } catch (Exception e) {
           log.error("send data fail", e);
        }
    }

    public void reconnect(Channel channel){
         if(!connectedChannels.contains(channel)){
             return;
         }
         InetSocketAddress ipSocket = (InetSocketAddress) channel.remoteAddress();
         if (ipSocket == null) {
             throw new NettyException("cannot connect to {}");
         }
         String host = ipSocket.getHostString();
         log.info("reconnect channel: {}", host);
         Channel newChannel = bootstrap.connect(host, config.getPort()).channel();
         connectedChannels.set(connectedChannels.indexOf(channel), newChannel);
    }
}
