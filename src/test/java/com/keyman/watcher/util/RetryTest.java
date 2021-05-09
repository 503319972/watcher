package com.keyman.watcher.util;

import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RetryTest {
    @Test
    public void retry(){
        Retry retry = Retry.retryAsync(() -> {
            System.out.println(1 / 0);
        }, 3, 1000);
        retry.waitForFinish();
        System.out.println("finish");
    }

    @Test
    public void retrySync() {
        Retry.retrySync(() -> {
            System.out.println(1 / 0);
        }, 3, 1000);
        System.out.println("finish");
    }

    @Test
    public void retrySync2() {
        try {
            System.out.println(1 / 0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}