package com.keyman.watcher.netty.strategy;

import com.keyman.watcher.netty.client.Client;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class LeaderCopyStrategy implements Strategy {
    @Override
    public void distribute(Client client, byte[] msg) {

    }

    @Override
    public BiConsumer<ChannelHandlerContext, Object> getServerHandler() {
        return null;
    }

    @Override
    public BiPredicate<ChannelHandlerContext, Object> getClientHandler() {
        return null;
    }
}
