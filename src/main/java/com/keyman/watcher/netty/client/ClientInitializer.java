package com.keyman.watcher.netty.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;
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
        pipeline.addLast(new StringEncoder()); // out
        pipeline.addLast(new ByteArrayDecoder()); // in
        pipeline.addLast(clientInHandler);
    }

    public void setDataHandler(Consumer<Object> handler) {
        clientInHandler.setDataHandler(handler);
    }

    public void setServerDownHandler(Consumer<Void> handler) {
        clientInHandler.setServerDownHandler(handler);
    }
}
