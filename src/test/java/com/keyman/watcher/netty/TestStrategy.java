package com.keyman.watcher.netty;

import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.netty.strategy.Strategy;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class TestStrategy implements Strategy {
    @Override
    public void distribute(Client client, byte[] msg) {

    }

    @Override
    public BiConsumer<ChannelHandlerContext, Object> getServerHandler() {
        return (ctx, obj) -> {
            ctx.writeAndFlush("server received");
            System.out.println(obj);
        };
    }

    @Override
    public BiPredicate<ChannelHandlerContext, Object> getClientHandler() {
        return (ctx, obj) -> {
            ctx.writeAndFlush("client received");
            System.out.println(obj);
            return true;
        };
    }
}
