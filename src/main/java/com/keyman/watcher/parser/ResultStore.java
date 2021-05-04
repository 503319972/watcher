package com.keyman.watcher.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResultStore {
    private ResultStore() {
    }

    private static final ConcurrentHashMap<String, Object> resultMap = new ConcurrentHashMap<>();

    public static void setGlobalResult(Map<String, ?> input) {
        resultMap.putAll(input);
    }

    public static Map<String, Object> getGlobalResult() {
        return new HashMap<>(resultMap);
    }
}
