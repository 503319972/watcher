package com.keyman.watcher.netty;

import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.util.Retry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class ConnectCenterTest {
    @Test
    public void test() {
//        ConnectCenter center = new ConnectCenter();
//        center.startServer((ctx, obj) -> {
//                ctx.writeAndFlush("pong".getBytes());
//            System.out.println(obj);
//        });
//        while (true) {}
    }

    @Test
    public void client() {
//        ConnectCenter center = new ConnectCenter();
        Client client = new Client();
        client.setHandler((ctx, obj) -> System.out.println(obj));
        client.start(true);
    }
    @Test
    public void client2() {
        Client client = new Client();
        client.setHandler((ctx, obj) -> System.out.println(obj));
        client.start(true);
    }
    @Test
    public void client3() {
        Client client = new Client();
        client.setHandler((ctx, obj) -> System.out.println(obj));
        client.start(true);
    }

    @Test
    public void client4() throws InterruptedException {
//        ConnectCenter center = new ConnectCenter();
//        center.startClient(null);
//        Thread.sleep(16000);
//        center.closeClient();
    }


    @Test
    public void waitt(){
        Thread thread = Thread.currentThread();
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.execute(() -> {
            try {
                Thread.sleep(2000);
                Thread.State state = thread.getState();
                System.out.println(state);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LockSupport.unpark(thread);
        });
        LockSupport.park();
        System.out.println("finish");
    }


    @Test
    public void retry() {
        Client client = new Client("123.33.22.11");
        client.start(true);
        Retry.retrySync(() -> {
            client.sendMsg(client.getConnectedChannels().get(0), "msg".getBytes());
        }, 3, 2000);
    }

    @Test
    public void multipleClient() throws InterruptedException {
        Client client = new Client(Arrays.asList("127.0.0.1", "127.0.0.1", "127.0.0.1"));
        client.start();
        List<Channel> channels = client.getConnectedChannels();
        Thread.sleep(5000);
        client.sendMsg(channels.get(0), "test1".getBytes());
    }
}