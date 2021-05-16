package com.keyman.watcher.netty;


import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.netty.strategy.StarCopyStrategy;
import com.keyman.watcher.parser.GlobalStore;
import com.keyman.watcher.util.Retry;
import io.netty.channel.Channel;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

public class ConnectCenterTest {
    @Test
    public void test() {
        ConnectCenter center = ConnectCenter.getInstance(new TestStrategy());
        center.startServer();
        while (true) {}
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
        List<String> hosts = Collections.singletonList("127.0.0.1");
        Client client = new Client(hosts);
        Retry.retrySync(() -> {
            client.start(true);
            client.sendMsg(hosts.get(0), "msg".getBytes());
        }, 5, 5000);
    }

    @Test
    public void multipleClient() throws InterruptedException {
        Client client = new Client(Arrays.asList("127.0.0.1", "127.0.0.1", "127.0.0.1"));
        client.start();
        List<String> channels = client.getConnectedChannels();
        Thread.sleep(5000);
        client.sendMsg(channels.get(0), "test1".getBytes());
    }

    @Test
    public void server1() {
        ConnectCenter center = ConnectCenter.getInstance(new StarCopyStrategy());
        center.startServer(new NettyConfig(56661));
        while (true) {}
    }

    @Test
    public void server2() {
        ConnectCenter center = ConnectCenter.getInstance(new StarCopyStrategy());
        center.startServer(new NettyConfig(56662));
        while (true) {}
    }

    private static final ConnectCenter CLIENT_CENTER = ConnectCenter.getInstance(new StarCopyStrategy());

    @Test
    public void clients() throws InterruptedException {
        CLIENT_CENTER.startClient(Arrays.asList("127.0.0.1:56661", "127.0.0.1:56662"));
        Thread.sleep(5000);
        HashMap<String, Object> map = new HashMap<>();
        map.put("1", "23");
        map.put("2", "4");
        GlobalStore.setLatestMap(map);
        CLIENT_CENTER.distributeCopy();
        while (true) {}
    }
}