package com.keyman.watcher.configuration;

import com.keyman.watcher.queue.LRUQueue;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class FileDirectoryListenerTest {

    @Test
    public void test() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
        executor.scheduleWithFixedDelay(() -> System.out.println("1"), 0, 1000, TimeUnit.MILLISECONDS);
        executor.scheduleWithFixedDelay(() -> System.out.println("2"), 0, 1000, TimeUnit.MILLISECONDS);
        while (true) {}
    }

    @Test
    public void test2() {
        LRUQueue<String> queue = new LRUQueue<>(10);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        pool.execute(() -> {
            queue.add("1");
            queue.add("2");
            queue.add("3");
        });

        pool.execute(() -> {
            queue.add("4");
            queue.add("5");
            queue.add("6");
        });

        pool.execute(() -> {
            queue.add("7");
            queue.add("8");
            queue.add("9");
        });

        pool.execute(() -> {
            queue.add("10");
            queue.add("11");
        });

        LockSupport.parkUntil(System.currentTimeMillis() + 1000);
        System.out.println(queue.get());
    }

}