package com.keyman.watcher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class Retry {
    private static final Logger log = LoggerFactory.getLogger(Retry.class);
    private static final ExecutorService pool = new ThreadPoolExecutor(10, 10, 2000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());
    private final TimeHolder holder;

    private Retry(TimeHolder holder) {
        this.holder = holder;
    }

    public static void retrySync(RetryFunction handler, int retry, long period) {
        retrySync(handler, retry, period, TimeUnit.MILLISECONDS);
    }

    public static void retrySync(RetryFunction handler, int retry, long period, TimeUnit timeUnit) {
        TimeHolder timeHolder = new TimeHolder(retry);
        int index = 1;
        while (!timeHolder.isFinish()) {
            try {
                handler.handle();
            } catch (Exception e) {
                log.warn("handler execute failed. Try time: {}", index++, e);
                timeHolder.tryAgain(period, timeUnit);
                continue;
            }
            timeHolder.finish();
        }
    }

    public static Retry retryAsync(RetryFunction handler, int retry, long period, TimeUnit timeUnit) {
        TimeHolder timeHolder = new TimeHolder(retry);
        Thread thread = Thread.currentThread();
        pool.execute(() -> {
            while (!timeHolder.isFinish()) {
                try {
                    handler.handle();
                } catch (Exception e) {
                    log.warn("handler execute failed. Try time: {}", timeHolder.times.get(), e);
                    timeHolder.tryAgain(period, timeUnit);
                    continue;
                }
                timeHolder.finish();
            }
            if (thread.getState().equals(Thread.State.WAITING)) {
                LockSupport.unpark(thread);
            }
        });
        return new Retry(timeHolder);
    }


    public static Retry retryAsync(RetryFunction handler) {
        return retryAsync(handler, 3, 0, null);
    }
    public static Retry retryAsync(RetryFunction handler , int retry) {
        return retryAsync(handler, retry, 0, null);
    }
    public static Retry retryAsync(RetryFunction handler , int retry, long period) {
        return retryAsync(handler, retry, period, null);
    }

    public void waitForFinish() {
        this.holder.waitForFinish();
    }



    public interface RetryFunction {
        void handle() throws Exception;
    }

    static class TimeHolder {
        private final int retryTimes;
        private final AtomicInteger times;
        private final AtomicBoolean finished = new AtomicBoolean(false);
        TimeHolder(int retry) {
            retryTimes = retry;
            times = new AtomicInteger(1);
        }
        void tryAgain(long period, TimeUnit timeUnit) {
            long l = Optional.ofNullable(timeUnit).map(t -> t.toMillis(period)).orElse(period);
            while (System.currentTimeMillis() < l + period) {}
            if (times.get() <= retryTimes)
                times.incrementAndGet();
        }

        boolean isFinish() {
            return times.get() == retryTimes + 1 || finished.get();
        }

        void finish() {
            finished.compareAndSet(false, true);
        }

        public void waitForFinish() {
            LockSupport.park();
        }
    }
}
