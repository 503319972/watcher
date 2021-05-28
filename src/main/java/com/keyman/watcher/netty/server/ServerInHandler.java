package com.keyman.watcher.netty.server;

import com.keyman.watcher.netty.ConnectCenter;
import com.keyman.watcher.netty.client.Client;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Sharable
class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ServerInHandler.class);
    private final List<String> hosts;
    private Consumer<ChannelHandlerContext> react;
    private final BiConsumer<ChannelHandlerContext, Object> biReact;
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public void setReact(Consumer<ChannelHandlerContext> react) {
        this.react = react;
    }

    ServerInHandler(BiConsumer<ChannelHandlerContext, Object> biReact, List<String> hosts) {
        this.hosts = hosts;
        this.biReact = biReact;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exception caught: {}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String address = channel.remoteAddress().toString();
        log.info("{} socket join in", address);
        channels.add(channel);
        String ip = address.replace("/", "").substring(address.indexOf(":"));
        boolean isNewHost = hosts.stream().noneMatch(host -> host.contains(ip));
        if (isNewHost) {
            ConnectCenter center = ConnectCenter.getInstance();
            Client client = center.getClient();
//            client.newConnect();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("{} socket remove", channel.remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        boolean reactP = Optional.ofNullable(react).isPresent();
        boolean biReactP = Optional.ofNullable(biReact).isPresent();
        if (reactP) {
            react.accept(ctx);
        }
        else if (biReactP) {
            biReact.accept(ctx, msg);
        }
    }
}
