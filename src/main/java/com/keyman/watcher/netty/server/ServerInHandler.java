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

    public BiConsumer<ChannelHandlerContext, Object> getBiReact() {
        return biReact;
    }

    public void setBiReact(BiConsumer<ChannelHandlerContext, Object> biReact) {
        this.biReact = biReact;
    }

    ServerInHandler() {
        this((BiConsumer<ChannelHandlerContext, Object>)null);
    }

    ServerInHandler(Consumer<ChannelHandlerContext> react) {
        this.react = react;
    }

    ServerInHandler(BiConsumer<ChannelHandlerContext, Object> biReact) {
        this.biReact = biReact;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("exception caught", cause);
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
        if (biReactP) {
            biReact.accept(ctx, msg);
        }
        else if (!reactP) {
            ctx.writeAndFlush("none handler");
        }
    }

//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception { // (4)
//        Channel incoming = ctx.channel();
//        for (Channel channel : channels) {//遍历ChannelGroup中的channel
//            if (channel != incoming){//找到加入到ChannelGroup中的channel后，将录入的信息回写给除去发送信息的客户端
//                channel.writeAndFlush("[" + incoming.remoteAddress() + "]" + s + "\n");
//            }
//            else {
//                channel.writeAndFlush("[you]" + s + "\n");
//            }
//        }
//    }
}
