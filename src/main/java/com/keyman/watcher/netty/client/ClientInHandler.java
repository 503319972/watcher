package com.keyman.watcher.netty.client;


import com.keyman.watcher.netty.ConnectCenter;
import com.keyman.watcher.parser.GlobalStore;
import com.keyman.watcher.util.Retry;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Sharable
public class ClientInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ClientInHandler.class);
    private BiConsumer<ChannelHandlerContext, Object> dataHandler;
    private Consumer<Void> serverDownHandler;

    public void setDataHandler(BiConsumer<ChannelHandlerContext,Object> dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void setServerDownHandler(Consumer<Void> serverDownHandler) {
        this.serverDownHandler = serverDownHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Optional.ofNullable(dataHandler).ifPresent(handler -> handler.accept(ctx, msg));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("error caught: {}", cause.getMessage());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE) && GlobalStore.getLatestMoreMapSent()) { // long time not receive
                ctx.writeAndFlush(GlobalStore.getLatestMoreMapForByte());
                log.info("no receive message after sending copy data, resend the copy data");
            }
        }
        super.userEventTriggered(ctx,evt);
    }

    // reconnect
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("cannot connect server, will reconnect soon");
        boolean netStatus = false;
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIP = inSocket.getAddress().getHostAddress();
        ctx.close();
        try {
            netStatus = InetAddress.getByName(clientIP).isReachable(1000);
        } catch (IOException e) {
            log.error("cannot ping outside: {}", clientIP);
        }
        if (netStatus) {
            ConnectCenter center = ConnectCenter.getInstance();
            Client client = center.getClient();
            Retry.retrySync(() -> client.reconnect(clientIP), 3, 2000);
        }
    }

    // channel be active
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channel connected");
    }
}