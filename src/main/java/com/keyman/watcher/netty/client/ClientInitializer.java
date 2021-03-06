package com.keyman.watcher.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class ClientInitializer extends ChannelInitializer<SocketChannel> {
    private final ClientInHandler clientInHandler = new ClientInHandler();

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("ping", new IdleStateHandler(10, 5,
                60 * 10L, TimeUnit.SECONDS)); // out
        pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0,
                2, 0, 2)); // in
        pipeline.addLast(new LengthFieldPrepender(2)); // out
        pipeline.addLast(new ByteArrayEncoder()); // out
        pipeline.addLast(new StringDecoder()); // in
        pipeline.addLast(clientInHandler);
    }

    public void setDataHandler(BiPredicate<ChannelHandlerContext, Object> dataHandler) {
        clientInHandler.setDataHandler(dataHandler);
    }

    public void setServerDownHandler(Consumer<Void> handler) {
        clientInHandler.setServerDownHandler(handler);
    }
}
