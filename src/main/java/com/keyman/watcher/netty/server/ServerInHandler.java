package com.keyman.watcher.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Sharable
class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ServerInHandler.class);
    private Consumer<ChannelHandlerContext> react;
    private BiConsumer<ChannelHandlerContext, Object> biReact;
    private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public Consumer<ChannelHandlerContext> getReact() {
        return react;
    }

    public void setReact(Consumer<ChannelHandlerContext> react) {
        this.react = react;
    }

    ServerInHandler(BiConsumer<ChannelHandlerContext, Object> biReact) {
        this.biReact = biReact;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exception caught, {}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("{} socket join in", channel.remoteAddress());
        channels.add(ctx.channel());
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
