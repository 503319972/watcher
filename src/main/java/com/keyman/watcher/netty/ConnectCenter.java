package com.keyman.watcher.netty;

import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.netty.server.Server;
import com.keyman.watcher.netty.strategy.Strategy;
import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.util.Retry;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ConnectCenter {
    private final ThreadPoolExecutor pool;
    private Strategy strategy;
    private Server server;
    private Client client;

    public Client getClient() {
        return client;
    }

    private ConnectCenter() {
        this.pool = new ThreadPoolExecutor(4, 4, 10000, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2));
    }

    public void startServer(){
        startServer(new NettyConfig());
    }

    public void startServer(NettyConfig nettyConfig){
        server = new Server(nettyConfig,
                Optional.ofNullable(strategy).map(Strategy::getServerHandler).orElse(null));
        pool.execute(() -> Retry.retrySyncInfinitely(server::start));
    }

    public void startClient(List<String> hosts){
        client = new Client(hosts);
        Optional.ofNullable(strategy).ifPresent(s -> client.setHandler(s.getClientHandler()));
        pool.execute(client::start);
    }

    public void startClient(NettyConfig config){
        startClient(config, null);
    }

    public void startClient(NettyConfig config, Consumer<Void> handler){
        client = new Client(config);
        Optional.ofNullable(strategy).ifPresent(s -> client.setHandler(s.getClientHandler()));
        pool.execute(() -> {
            client.start();
            Optional.ofNullable(handler).ifPresent(h -> h.accept(null));
        });
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

    public void distributeCopy() {
        strategy.distribute(client, GlobalStore.getLatestMoreMapForByte());
    }

    private static class Holder {
        private static final ConnectCenter CENTER = new ConnectCenter();
    }

    public static ConnectCenter getInstance(Strategy strategy)
    {
        if (Holder.CENTER.strategy == null) {
            Holder.CENTER.strategy = strategy;
        }
        return Holder.CENTER;
    }

    public static ConnectCenter getInstance()
    {
        return Holder.CENTER;
    }
}
