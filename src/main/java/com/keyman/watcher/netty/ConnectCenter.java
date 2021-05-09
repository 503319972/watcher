package com.keyman.watcher.netty;

import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.netty.server.Server;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class ConnectCenter {
    private final ThreadPoolExecutor pool;
    private Server server;
    private Client client;

    public Client getClient() {
        return client;
    }

    public ConnectCenter() {
        this.pool = new ThreadPoolExecutor(4, 4, 10000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2));
    }

    public void startServer(BiConsumer<ChannelHandlerContext, Object> react){
        server = new Server(new NettyConfig(), react);
        pool.execute(server::start);
    }

    public void startClient(Consumer<Object> handler){
        client = new Client();
        client.setHandler(handler);
        pool.execute(client::start);
    }

    public void closeClient(){
        pool.execute(client::close);
    }

    public void setServerDownHandler(Consumer<Void> handler){
        client.setServerDownHandler(handler);
    }

    public void closeAServer() {
        pool.execute(server::close);
    }
}
