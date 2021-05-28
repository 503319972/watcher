package com.keyman.watcher.netty;


import com.keyman.watcher.netty.client.Client;
import com.keyman.watcher.netty.strategy.StarCopyStrategy;
import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.util.Retry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
        client.setHandler((ctx, obj) -> {
            System.out.println(obj);
            return true;
        });
        client.start(true);
    }
    @Test
    public void client2() {
        Client client = new Client();
        client.setHandler((ctx, obj) -> {
            System.out.println(obj);
            return true;
        });
        client.start(true);
    }
    @Test
    public void client3() {
        Client client = new Client();
        client.setHandler((ctx, obj) -> {
            System.out.println(obj);
            return true;
        });
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
    private static final Logger log = LoggerFactory.getLogger(ConnectCenterTest.class);
    @Test
    public void clients() throws InterruptedException {
        CLIENT_CENTER.startClient(Arrays.asList("127.0.0.1:56661", "127.0.0.1:56662"));
        Thread.sleep(5000);
        HashMap<String, Object> map = new HashMap<>();
        map.put("1", "23");
        map.put("2", "4");
        GlobalStore.setLatestMap(Collections.emptyMap(), true);
        CLIENT_CENTER.distributeCopy();
        while (true) {}
    }

    @Test
    public void clientSingle() {
        CLIENT_CENTER.startClient(Arrays.asList("127.0.0.1:56661"));
        while (true) {}
    }

    @Test
    public void sys() throws UnknownHostException {
        InetAddress address = InetAddress.getLoopbackAddress();
        String hostAddress = address.getHostAddress();
        InetAddress localHost = InetAddress.getLocalHost();
        String localHostHostAddress = localHost.getHostAddress();
        System.out.println(localHostHostAddress);
        System.out.println(hostAddress);

//        String k = "123.json";
//        String substring = k.substring("".length() + 1, k.lastIndexOf('.'));
//        substring = k.substring(1);
//        System.out.println(substring);
    }

    @Test
    public void concurrent() {
        Map<String, Map<String, Object>> resultMap = new ConcurrentHashMap<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("1", "1");
        map.put("2", "2");
        resultMap.put("1", map);



        resultMap.computeIfPresent("1", (k, v) -> {
            v.put("3", "3");
            return v;
        });
    }

    @Test
    public void concurrent2() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, executor);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, executor);
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, executor);

        future.thenAcceptBothAsync(future2, (voi1, voi2) -> {
            System.out.println("finish");
                }, executor).thenApplyAsync(voi -> {
            System.out.println("finish2");
            return null;
        }, executor);



        while (true) {}
    }

    @Test
    public void concurrent3() {
        ScheduledThreadPoolExecutor pool = new ScheduledThreadPoolExecutor(2);
        pool.scheduleWithFixedDelay(() -> {
            System.out.println("come1");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);

        ScheduledThreadPoolExecutor pool2 = new ScheduledThreadPoolExecutor(2);
        pool2.scheduleAtFixedRate(() -> {
            System.out.println("come2");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.SECONDS);
        while (true) {}
    }
}