package com.keyman.watcher.parser;

import com.keyman.watcher.queue.LRUQueue;
import com.keyman.watcher.util.GZIPUtil;
import com.keyman.watcher.util.JsonUtil;
import com.keyman.watcher.util.Retry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalStore {
    private GlobalStore() {
    }

    private static final ConcurrentHashMap<String, Object> resultMap = new ConcurrentHashMap<>();
    private static ObjectHolder<Map<String, Object>, Boolean> LATEST_MORE_MAP = ObjectHolder.of(null, true);
    private static boolean jarBoot = false;
    private static final LRUQueue<Long> UPDATE_TIME_SERIES = new LRUQueue<>(10);


    public static void putGlobalResult(Map<String, ?> input) {
        if (input != null && input.size() > 0) {
            resultMap.putAll(input);
            Retry.retryAsync(GlobalStore::putTimeFlag);
        }
    }

    public static void setLatestMoreMap(Map<String, Object> input) {
        if (input != null && input.size() > 0) {
            while (Boolean.FALSE.equals(LATEST_MORE_MAP.getRight())) { }
            LATEST_MORE_MAP.putLeft(input);
            LATEST_MORE_MAP.putRight(false);
            Retry.retryAsync(GlobalStore::putTimeFlag);
        }
    }

    public static void setLatestMoreMapSent() {
        LATEST_MORE_MAP.putRight(true);
    }

    public static boolean getLatestMoreMapSent() {
        return LATEST_MORE_MAP.getRight();
    }

    public static Map<String, Object> getLatestMoreMap() {
        return LATEST_MORE_MAP.getLeft();
    }

    public static byte[] getLatestMoreMapForByte() {
        return GZIPUtil.compress(JsonUtil.writeToByte(LATEST_MORE_MAP.getLeft()));
    }

    private static void putTimeFlag() {
        UPDATE_TIME_SERIES.add(System.currentTimeMillis());
    }

    public static Long getLatestTime(){
        return UPDATE_TIME_SERIES.get();
    }

    public static Map<String, Object> getGlobalResultCopy() {
        return new HashMap<>(resultMap);
    }

    public static Map<String, Object> getGlobalResult() {
        return resultMap;
    }

    public static void setJarBoot() {
        jarBoot = true;
    }

    public static boolean getJarBoot() {
        return jarBoot;
    }
}
