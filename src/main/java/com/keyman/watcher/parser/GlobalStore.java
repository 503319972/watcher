package com.keyman.watcher.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalStore {
    private GlobalStore() {
    }

    private static final ConcurrentHashMap<String, Object> resultMap = new ConcurrentHashMap<>();
    private static boolean JAR_BOOT = false;

    public static void setGlobalResult(Map<String, ?> input) {
        if (input != null && input.size() > 0) {
            resultMap.putAll(input);
        }
    }

    public static Map<String, Object> getGlobalResult() {
        return new HashMap<>(resultMap);
    }

    public static void setJarBoot() {
        JAR_BOOT = true;
    }

    public static boolean getJarBoot() {
        return JAR_BOOT;
    }
}
