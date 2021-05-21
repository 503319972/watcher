package com.keyman.watcher.netty.strategy;

import com.keyman.watcher.netty.client.Client;
import io.netty.channel.ChannelHandlerContext;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public interface Strategy {
    void distribute(Client client, byte[] msg);
    BiConsumer<ChannelHandlerContext, Object> getServerHandler();
    BiPredicate<ChannelHandlerContext, Object> getClientHandler();
}
