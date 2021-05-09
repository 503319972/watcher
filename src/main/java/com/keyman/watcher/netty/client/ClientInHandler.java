package com.keyman.watcher.netty.client;


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
import java.util.function.Consumer;

@Sharable
public class ClientInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ClientInHandler.class);
    private Consumer<Object> dataHandler;
    private Consumer<Void> serverDownHandler;

    public void setDataHandler(Consumer<Object> dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void setServerDownHandler(Consumer<Void> serverDownHandler) {
        this.serverDownHandler = serverDownHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Optional.ofNullable(dataHandler).ifPresent(handler -> handler.accept(msg));
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

            if (event.state().equals(IdleState.READER_IDLE)) { // long time not receive
//                log.warn("long time no receive");
            } else if (event.state().equals(IdleState.WRITER_IDLE)) { // send a patch to server every single time
                ctx.writeAndFlush("ping");
            } else if (event.state().equals(IdleState.ALL_IDLE)) { // not above situation
                log.error("unknown idle state");
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
            Client client = new Client(clientIP);
            Retry.retrySync(() -> client.start(true), 3, 2000);
            if (!client.isStart()) {
                Optional.ofNullable(serverDownHandler).ifPresent(t -> t.accept(null));
            }
        }
    }

    // channel be active
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channel connected");
    }
}