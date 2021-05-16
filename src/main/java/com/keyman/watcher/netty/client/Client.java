package com.keyman.watcher.netty.client;

import com.keyman.watcher.exception.NettyException;
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
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
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

    public void setHandler(BiConsumer<ChannelHandlerContext,Object> dataHandler){
        clientInitializer.setDataHandler(dataHandler);
    }

    public void setServerDownHandler(Consumer<Void> handler){
        clientInitializer.setServerDownHandler(handler);
    }

    public void toStart() {
        flush();
        List<Retry> retries = new ArrayList<>();
        config.getHosts().forEach(ip -> {
            String[] ipPort = ip.split(":");
            bootstrap.remoteAddress(ipPort.length > 1 ? ipPort[0] : ip, ipPort.length > 1 ?
                    Integer.parseInt(ipPort[1]) : config.getPort());

            Retry retry = Retry.retryAsync(() -> {
                ChannelFuture future = bootstrap.connect().sync();
                connectedChannelsMap.put(ip, future.channel());
            });
            retries.add(retry);
//            try {
//                ChannelFuture future = bootstrap.connect().sync();
//                connectedChannelsMap.put(ip, future.channel());
//            } catch (Exception e) {
//                throw new NettyException("init channel error", e);
//            }
        });
        retries.forEach(retry -> retry.waitForFinish());
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
        if (channel == null) {
            throw new NettyException("have not build connect to "  + ip + " yet");
        }
        if (!channel.isActive()) {
            reconnect(ip);
        }
        try {
            if (channel.isActive()) {
                InetSocketAddress ipSocket = (InetSocketAddress) channel.remoteAddress();
                String host = ipSocket.getHostString();
                int curPort = ipSocket.getPort();
                ByteBuf buf = Unpooled.buffer();
                buf.writeBytes(msg);
                channel.writeAndFlush(buf).sync();
                log.info("send data to {}:{}", host , curPort);
            }
        } catch (Exception e) {
            log.error("send data fail", e);
        }
    }

    public void reconnect(String ip) {
        if(!connectedChannelsMap.containsKey(ip)){
            return;
        }
        Channel newChannel;
        InetSocketAddress ipSocket;
        try {
            newChannel = bootstrap.connect(ip, config.getPort()).sync().channel();
            ipSocket = (InetSocketAddress) newChannel.remoteAddress();
        } catch (Exception e) {
            throw new NettyException("cannot connect to channel " + ip);
        }
        String host = ipSocket.getHostString();
        log.info("reconnect channel: {}", host);
        connectedChannelsMap.put(ip, newChannel);
    }
}
