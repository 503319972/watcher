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
    private static final ObjectHolder<Map<String, Object>, Boolean> LATEST_MAP = ObjectHolder.of(null, true);
    private static boolean jarBoot = false;
    private static final LRUQueue<Long> UPDATE_TIME_SERIES = new LRUQueue<>(10);


    public static void putGlobalResult(Map<String, ?> input) {
        if (input != null && input.size() > 0) {
            resultMap.putAll(input);
            Retry.tryAsync(GlobalStore::putTimeFlag);
        }
    }

    public static void setLatestMap(Map<String, Object> input) {
        setLatestMap(input, false);
    }

    public static void setLatestMap(Map<String, Object> input, boolean async) {
        if (input != null && input.size() > 0) {
            if (async) {
                Retry.tryAsync(() -> addLatestMap(input));
            } else {
                addLatestMap(input);
            }
        }
    }

    private static void addLatestMap(Map<String, Object> input) {
        while (Boolean.FALSE.equals(LATEST_MAP.getRight())) { }
        LATEST_MAP.putLeft(input);
        LATEST_MAP.putRight(false);
        putTimeFlag();
    }

    public static void setLatestMoreMapSent() {
        LATEST_MAP.putRight(true);
    }

    public static boolean getLatestMoreMapSent() {
        return LATEST_MAP.getRight() && LATEST_MAP.getLeft() != null && LATEST_MAP.getLeft().size() > 0;
    }

    public static Map<String, Object> getLatestMap() {
        return LATEST_MAP.getLeft();
    }

    public static byte[] getLatestMoreMapForByte() {
        return GZIPUtil.compress(JsonUtil.writeToByte(LATEST_MAP.getLeft()));
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
