package com.keyman.watcher.netty.client;

import com.keyman.watcher.exception.NettyException;
import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.netty.NettyConfig;
import com.keyman.watcher.util.Retry;
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    private final Bootstrap bootstrap = new Bootstrap();
    private EventLoopGroup worker;
    private volatile boolean start = false;
    private final ConcurrentHashMap<String, Channel> connectedChannelsMap = new ConcurrentHashMap<>();
    private final NettyConfig config;
    private final ClientInitializer clientInitializer;

    public EventLoopGroup getWorker() {
        return worker;
    }

    public List<String> getConnectedChannels() {
        return new ArrayList<>(connectedChannelsMap.keySet());
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

    public void setHandler(BiPredicate<ChannelHandlerContext, Object> dataHandler){
        clientInitializer.setDataHandler(dataHandler);
    }

    public void setServerDownHandler(Consumer<Void> handler){
        clientInitializer.setServerDownHandler(handler);
    }

    public void toStart() {
        flush();
        config.getHosts().forEach(ip -> {
            if (!ip.equals(GlobalStore.getLocalHost()) &&
                    !ip.equals(GlobalStore.getHost())){
                ChannelFuture connect = connect(ip);
                Channel channel = connect.channel();
                connectedChannelsMap.put(ip, channel);
                if (!channel.isActive()) {
                    Retry.retryAsync(() -> {
                        ChannelFuture future = connect(ip).sync();
                        connectedChannelsMap.put(ip, future.channel());
                    }, 5, 5000).waitForFinish();
                }
            }
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

    public void sendMsg(@NotNull String ip, byte[] msg) {
        Channel channel = connectedChannelsMap.get(ip);
        try {
            if (!channel.isActive()) {
                reconnect(ip);
            }
            else {
                InetSocketAddress ipSocket = (InetSocketAddress) channel.remoteAddress();
                String host = ipSocket.getHostString();
                int curPort = ipSocket.getPort();
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg);
                channel.writeAndFlush(buf).sync();
                log.info("send data to {}:{}", host , curPort);
            }
        } catch (Exception e) {
            log.error("send data fail, {}", e.getMessage());
        }
    }

    public void reconnect(String ip) {
        if(!connectedChannelsMap.containsKey(ip)){
            return;
        }
        Channel newChannel;
        InetSocketAddress ipSocket;
        try {
            newChannel = connect(ip).sync().channel();
            ipSocket = (InetSocketAddress) newChannel.remoteAddress();
        } catch (Exception e) {
            throw new NettyException("cannot connect to channel " + ip);
        }
        String host = ipSocket.getHostString();
        log.info("reconnect channel: {}", host);
        connectedChannelsMap.put(ip, newChannel);
    }

    private ChannelFuture connect(String host){
        String[] ipPort = host.split(":");
        return bootstrap.connect(ipPort.length > 1 ? ipPort[0] : host, ipPort.length > 1 ?
                Integer.parseInt(ipPort[1]) : config.getPort());
    }
}
